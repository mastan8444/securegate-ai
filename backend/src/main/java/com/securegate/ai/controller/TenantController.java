package com.securegate.ai.controller;

import com.securegate.ai.entity.Tenant;
import com.securegate.ai.repository.TenantRepository;
import com.securegate.ai.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantRepository tenantRepository;
    private final RuleService ruleService;

    public TenantController(TenantRepository tenantRepository, RuleService ruleService) {
        this.tenantRepository = tenantRepository;
        this.ruleService = ruleService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String planType = body.getOrDefault("planType", "FREE").toUpperCase();

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Organization name is required");
        }

        // Generate UUID based unique tenantId
        String tenantId = "tenant_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        
        // Generate a cryptographically secure random API Key
        String apiKey = "sg_live_" + UUID.randomUUID().toString().replace("-", "");

        // Create and save the new Tenant
        Tenant newTenant = new Tenant(tenantId, name, apiKey, planType);
        tenantRepository.save(newTenant);

        // Seed default security rules for this tenant (so they have baseline thresholds out-of-the-box)
        ruleService.seedTenantRules(tenantId);

        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "name", name,
                "apiKey", apiKey,
                "planType", planType,
                "message", "Tenant registered successfully! Please store your API key safely."
        ));
    }
}
