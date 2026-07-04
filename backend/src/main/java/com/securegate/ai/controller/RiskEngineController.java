package com.securegate.ai.controller;

import com.securegate.ai.entity.RiskAssessment;
import com.securegate.ai.entity.RiskModuleConfig;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.RiskAssessmentRepository;
import com.securegate.ai.repository.RiskModuleConfigRepository;
import com.securegate.ai.repository.UserRepository;
import com.securegate.ai.service.RiskEngineService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risk")
public class RiskEngineController {

    private final RiskModuleConfigRepository configRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final RiskEngineService riskEngineService;

    public RiskEngineController(RiskModuleConfigRepository configRepository,
                                RiskAssessmentRepository assessmentRepository,
                                UserRepository userRepository,
                                RiskEngineService riskEngineService) {
        this.configRepository = configRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.riskEngineService = riskEngineService;
    }

    private String getCurrentTenantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getTenantId)
                .orElse("system");
    }

    @GetMapping("/configs")
    public ResponseEntity<List<RiskModuleConfig>> getConfigs() {
        String tenantId = getCurrentTenantId();
        // Ensure configuration is seeded
        riskEngineService.seedDefaultConfigs(tenantId);
        return ResponseEntity.ok(configRepository.findAllByTenantId(tenantId));
    }

    @PutMapping("/configs/{moduleKey}")
    public ResponseEntity<?> updateConfig(@PathVariable String moduleKey, @RequestBody Map<String, Object> body) {
        String tenantId = getCurrentTenantId();
        RiskModuleConfig config = configRepository.findByTenantIdAndModuleKey(tenantId, moduleKey)
                .orElseThrow(() -> new IllegalArgumentException("Module config not found for key: " + moduleKey));

        if (body.containsKey("enabled")) {
            config.setEnabled((Boolean) body.get("enabled"));
        }
        if (body.containsKey("riskWeight")) {
            config.setRiskWeight((Integer) body.get("riskWeight"));
        }
        if (body.containsKey("thresholds")) {
            config.setThresholds((String) body.get("thresholds"));
        }

        configRepository.save(config);
        riskEngineService.syncCache(tenantId);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/assessments")
    public ResponseEntity<List<RiskAssessment>> getAssessments() {
        String tenantId = getCurrentTenantId();
        return ResponseEntity.ok(assessmentRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, 100)));
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        String tenantId = getCurrentTenantId();
        List<RiskAssessment> assessments = assessmentRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId, PageRequest.of(0, 500));

        long totalAssessments = assessments.size();
        long totalAllowed = assessments.stream().filter(a -> "ALLOW".equals(a.getActionTaken())).count();
        long totalLogged = assessments.stream().filter(a -> "LOG".equals(a.getActionTaken())).count();
        long totalCaptcha = assessments.stream().filter(a -> "CAPTCHA".equals(a.getActionTaken())).count();
        long totalMfa = assessments.stream().filter(a -> "MFA".equals(a.getActionTaken())).count();
        long totalBlocked = assessments.stream().filter(a -> "BLOCK".equals(a.getActionTaken())).count();

        // Calculate top attack vectors/modules from details JSON
        Map<String, Long> attackTypeCounts = new HashMap<>();
        for (RiskAssessment assessment : assessments) {
            String details = assessment.getDetails();
            if (details != null && !details.isEmpty()) {
                if (details.contains("SQL_INJECTION")) attackTypeCounts.merge("SQL Injection", 1L, Long::sum);
                if (details.contains("XSS")) attackTypeCounts.merge("XSS", 1L, Long::sum);
                if (details.contains("PATH_TRAVERSAL")) attackTypeCounts.merge("Path Traversal", 1L, Long::sum);
                if (details.contains("COMMAND_INJECTION")) attackTypeCounts.merge("Command Injection", 1L, Long::sum);
                if (details.contains("BRUTE_FORCE")) attackTypeCounts.merge("Brute Force", 1L, Long::sum);
                if (details.contains("RATE_LIMITING")) attackTypeCounts.merge("Rate Limiting", 1L, Long::sum);
                if (details.contains("BOT_DETECTION")) attackTypeCounts.merge("Bot Traffic", 1L, Long::sum);
                if (details.contains("TOR_EXIT_NODE")) attackTypeCounts.merge("Tor Proxy Access", 1L, Long::sum);
                if (details.contains("GEOLOCATION")) attackTypeCounts.merge("Geoblock Violation", 1L, Long::sum);
            }
        }

        // Map top IPs
        Map<String, Long> ipCounts = assessments.stream()
                .collect(Collectors.groupingBy(RiskAssessment::getIpAddress, Collectors.counting()));

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalAssessments", totalAssessments);
        metrics.put("actionDistribution", Map.of(
            "ALLOW", totalAllowed,
            "LOG", totalLogged,
            "CAPTCHA", totalCaptcha,
            "MFA", totalMfa,
            "BLOCK", totalBlocked
        ));
        metrics.put("topAttacks", attackTypeCounts);
        metrics.put("topIps", ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return ResponseEntity.ok(metrics);
    }
}
