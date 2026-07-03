package com.securegate.ai.controller;

import com.securegate.ai.entity.Rule;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.UserRepository;
import com.securegate.ai.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;
    private final UserRepository userRepository;

    public RuleController(RuleService ruleService, UserRepository userRepository) {
        this.ruleService = ruleService;
        this.userRepository = userRepository;
    }

    private String getCurrentTenantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getTenantId)
                .orElse(RuleService.DEFAULT_TENANT_ID);
    }

    @GetMapping
    public ResponseEntity<List<Rule>> getRules() {
        String tenantId = getCurrentTenantId();
        return ResponseEntity.ok(ruleService.getAllRules(tenantId));
    }

    @PutMapping("/{key}")
    public ResponseEntity<?> updateRule(@PathVariable String key, @RequestBody Map<String, Object> body) {
        String value = String.valueOf(body.get("ruleValue"));
        boolean enabled = (boolean) body.get("enabled");

        String tenantId = getCurrentTenantId();
        try {
            Rule updated = ruleService.updateRule(tenantId, key, value, enabled);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
