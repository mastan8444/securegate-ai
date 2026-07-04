package com.securegate.ai.controller;

import com.securegate.ai.entity.BlacklistIP;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.BlacklistRepository;
import com.securegate.ai.repository.UserRepository;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    private final BlacklistRepository blacklistRepository;
    private final DecisionEngine decisionEngine;
    private final UserRepository userRepository;

    public BlacklistController(BlacklistRepository blacklistRepository,
                               DecisionEngine decisionEngine,
                               UserRepository userRepository) {
        this.blacklistRepository = blacklistRepository;
        this.decisionEngine = decisionEngine;
        this.userRepository = userRepository;
    }

    private String getCurrentTenantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getTenantId)
                .orElse(RuleService.DEFAULT_TENANT_ID);
    }

    @GetMapping
    public ResponseEntity<List<BlacklistIP>> getAllBlacklistedIps() {
        String tenantId = getCurrentTenantId();
        return ResponseEntity.ok(blacklistRepository.findAllByTenantId(tenantId));
    }

    @PostMapping
    public ResponseEntity<?> addIpToBlacklist(@RequestBody Map<String, String> body) {
        String ip = body.get("ipAddress");
        String reason = body.getOrDefault("reason", "Manually Blocked");
        String status = body.getOrDefault("status", "PERMANENT"); // TEMPORARY, PERMANENT
        String durationHoursStr = body.get("durationHours");

        if (ip == null || ip.isBlank()) {
            return ResponseEntity.badRequest().body("ipAddress is required");
        }

        LocalDateTime expiryTime = null;
        if ("TEMPORARY".equals(status)) {
            int hours = 24;
            if (durationHoursStr != null && !durationHoursStr.isBlank()) {
                try {
                    hours = Integer.parseInt(durationHoursStr);
                } catch (NumberFormatException e) {
                    // Default to 24
                }
            }
            expiryTime = LocalDateTime.now().plusHours(hours);
        }

        String tenantId = getCurrentTenantId();
        decisionEngine.blockIP(tenantId, ip, reason, status, expiryTime);
        return ResponseEntity.ok(Map.of("message", "IP blacklisted successfully", "ip", ip));
    }

    /**
     * Unblocks an IP address. Uses regex to capture IP dots correctly in path variables.
     */
    @DeleteMapping("/{ip:.+}")
    public ResponseEntity<?> removeIpFromBlacklist(@PathVariable String ip) {
        if (ip == null || ip.isBlank()) {
            return ResponseEntity.badRequest().body("IP address is required");
        }

        String tenantId = getCurrentTenantId();
        
        // 1. Resolve current user entity
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        // 2. Check if the IP is permanently blocked
        Optional<BlacklistIP> blacklistEntry = blacklistRepository.findByTenantIdAndIpAddress(tenantId, ip);
        if (blacklistEntry.isPresent()) {
            BlacklistIP entry = blacklistEntry.get();
            if ("PERMANENT".equals(entry.getStatus())) {
                boolean isSuperAdmin = currentUser != null && "ROLE_SUPER_ADMIN".equals(currentUser.getRole());
                boolean isSystemTenant = "system".equals(tenantId);
                
                if (!isSuperAdmin && !isSystemTenant) {
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "Forbidden",
                        "message", "Only SecureGate AI Platform super-administrators can unblock permanently blacklisted IPs. Contact support."
                    ));
                }
            }
        }

        decisionEngine.unblockIP(tenantId, ip);
        return ResponseEntity.ok(Map.of("message", "IP unblocked successfully", "ip", ip));
    }
}
