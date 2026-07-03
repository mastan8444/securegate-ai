package com.securegate.ai.controller;

import com.securegate.ai.entity.WhitelistIP;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.WhitelistRepository;
import com.securegate.ai.repository.UserRepository;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whitelist")
public class WhitelistController {

    private final WhitelistRepository whitelistRepository;
    private final DecisionEngine decisionEngine;
    private final UserRepository userRepository;

    public WhitelistController(WhitelistRepository whitelistRepository,
                               DecisionEngine decisionEngine,
                               UserRepository userRepository) {
        this.whitelistRepository = whitelistRepository;
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
    public ResponseEntity<List<WhitelistIP>> getAllWhitelistedIps() {
        String tenantId = getCurrentTenantId();
        return ResponseEntity.ok(whitelistRepository.findAllByTenantId(tenantId));
    }

    @PostMapping
    public ResponseEntity<?> addIpToWhitelist(@RequestBody Map<String, String> body) {
        String ip = body.get("ipAddress");
        String owner = body.getOrDefault("owner", "System Admin");

        if (ip == null || ip.isBlank()) {
            return ResponseEntity.badRequest().body("ipAddress is required");
        }

        String tenantId = getCurrentTenantId();
        decisionEngine.whitelistIP(tenantId, ip, owner);
        return ResponseEntity.ok(Map.of("message", "IP whitelisted successfully", "ip", ip));
    }

    /**
     * Removes an IP from the whitelist. Uses regex to capture IP dots correctly in path variables.
     */
    @DeleteMapping("/{ip:.+}")
    public ResponseEntity<?> removeIpFromWhitelist(@PathVariable String ip) {
        if (ip == null || ip.isBlank()) {
            return ResponseEntity.badRequest().body("IP address is required");
        }

        String tenantId = getCurrentTenantId();
        decisionEngine.removeFromWhitelist(tenantId, ip);
        return ResponseEntity.ok(Map.of("message", "IP removed from whitelist", "ip", ip));
    }
}
