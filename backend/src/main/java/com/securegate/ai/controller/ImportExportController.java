package com.securegate.ai.controller;

import com.securegate.ai.entity.BlacklistIP;
import com.securegate.ai.entity.WhitelistIP;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.BlacklistRepository;
import com.securegate.ai.repository.WhitelistRepository;
import com.securegate.ai.repository.UserRepository;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.RuleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImportExportController {

    private final DecisionEngine decisionEngine;
    private final BlacklistRepository blacklistRepository;
    private final WhitelistRepository whitelistRepository;
    private final UserRepository userRepository;

    public ImportExportController(DecisionEngine decisionEngine,
                                  BlacklistRepository blacklistRepository,
                                  WhitelistRepository whitelistRepository,
                                  UserRepository userRepository) {
        this.decisionEngine = decisionEngine;
        this.blacklistRepository = blacklistRepository;
        this.whitelistRepository = whitelistRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentTenantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getTenantId)
                .orElse(RuleService.DEFAULT_TENANT_ID);
    }

    /**
     * Export Blacklist as CSV.
     */
    @GetMapping("/export/blacklist")
    public ResponseEntity<byte[]> exportBlacklistCsv() {
        String tenantId = getCurrentTenantId();
        List<BlacklistIP> list = blacklistRepository.findAllByTenantId(tenantId);
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ipAddress,reason,status,blockedAt,expiryTime\n");

        for (BlacklistIP ip : list) {
            csvContent.append(ip.getIpAddress()).append(",")
                    .append(ip.getReason().replace(",", ";")).append(",")
                    .append(ip.getStatus()).append(",")
                    .append(ip.getBlockedAt()).append(",")
                    .append(ip.getExpiryTime() != null ? ip.getExpiryTime() : "").append("\n");
        }

        byte[] csvBytes = csvContent.toString().getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blacklist.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    /**
     * Import Blacklist from CSV.
     */
    @PostMapping("/import/blacklist")
    public ResponseEntity<?> importBlacklistCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid CSV file");
        }

        String tenantId = getCurrentTenantId();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int count = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");
                // Check if it's the header row
                if (isHeader && (columns[0].equalsIgnoreCase("ipAddress") || columns[0].equalsIgnoreCase("ip"))) {
                    isHeader = false;
                    continue;
                }
                isHeader = false;

                if (columns.length > 0) {
                    String ip = columns[0].trim();
                    String reason = columns.length > 1 ? columns[1].trim() : "Imported from CSV";
                    String status = columns.length > 2 ? columns[2].trim().toUpperCase() : "PERMANENT";
                    
                    if (!"TEMPORARY".equals(status) && !"PERMANENT".equals(status)) {
                        status = "PERMANENT";
                    }

                    decisionEngine.blockIP(tenantId, ip, reason, status, null);
                    count++;
                }
            }

            return ResponseEntity.ok(Map.of("message", "Blacklist imported successfully", "importedCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error reading CSV file: " + e.getMessage());
        }
    }

    /**
     * Export Whitelist as CSV.
     */
    @GetMapping("/export/whitelist")
    public ResponseEntity<byte[]> exportWhitelistCsv() {
        String tenantId = getCurrentTenantId();
        List<WhitelistIP> list = whitelistRepository.findAllByTenantId(tenantId);
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ipAddress,owner,addedAt\n");

        for (WhitelistIP ip : list) {
            csvContent.append(ip.getIpAddress()).append(",")
                    .append(ip.getOwner().replace(",", ";")).append(",")
                    .append(ip.getAddedAt()).append("\n");
        }

        byte[] csvBytes = csvContent.toString().getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=whitelist.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    /**
     * Import Whitelist from CSV.
     */
    @PostMapping("/import/whitelist")
    public ResponseEntity<?> importWhitelistCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid CSV file");
        }

        String tenantId = getCurrentTenantId();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int count = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");
                if (isHeader && (columns[0].equalsIgnoreCase("ipAddress") || columns[0].equalsIgnoreCase("ip"))) {
                    isHeader = false;
                    continue;
                }
                isHeader = false;

                if (columns.length > 0) {
                    String ip = columns[0].trim();
                    String owner = columns.length > 1 ? columns[1].trim() : "Imported Owner";

                    decisionEngine.whitelistIP(tenantId, ip, owner);
                    count++;
                }
            }

            return ResponseEntity.ok(Map.of("message", "Whitelist imported successfully", "importedCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error reading CSV file: " + e.getMessage());
        }
    }
}
