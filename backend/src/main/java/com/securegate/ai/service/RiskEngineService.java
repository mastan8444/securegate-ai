package com.securegate.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securegate.ai.dto.RiskContribution;
import com.securegate.ai.dto.RiskDecision;
import com.securegate.ai.dto.VisitorMetadata;
import com.securegate.ai.entity.RiskAssessment;
import com.securegate.ai.entity.RiskModuleConfig;
import com.securegate.ai.repository.RiskAssessmentRepository;
import com.securegate.ai.repository.RiskModuleConfigRepository;
import com.securegate.ai.service.module.SecurityDetectionModule;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.Lazy;
import com.securegate.ai.entity.BlacklistIP;
import com.securegate.ai.repository.BlacklistRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskEngineService {

    private final List<SecurityDetectionModule> modules;
    private final RiskModuleConfigRepository configRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final BlacklistRepository blacklistRepository;
    private final DecisionEngine decisionEngine;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Local high-performance config cache
    private final Map<String, RiskModuleConfig> localCache = new ConcurrentHashMap<>();
    private boolean redisAvailable = true;

    public RiskEngineService(List<SecurityDetectionModule> modules,
                             RiskModuleConfigRepository configRepository,
                             RiskAssessmentRepository assessmentRepository,
                             BlacklistRepository blacklistRepository,
                             @Lazy DecisionEngine decisionEngine,
                             RedisTemplate<String, String> redisTemplate) {
        this.modules = modules;
        this.configRepository = configRepository;
        this.assessmentRepository = assessmentRepository;
        this.blacklistRepository = blacklistRepository;
        this.decisionEngine = decisionEngine;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        testRedis();
        // Seed default config for system tenant on start
        seedDefaultConfigs("system");
    }

    private void testRedis() {
        try {
            redisTemplate.opsForValue().set("securegate:risk:ping", "pong");
            redisAvailable = true;
            System.out.println("[Risk Engine] Redis connection verified.");
        } catch (Exception e) {
            redisAvailable = false;
            System.out.println("[Risk Engine] Redis offline. Using JVM memory caches.");
        }
    }

    /**
     * Seeds initial configurations for all modules if missing.
     */
    public void seedDefaultConfigs(String tenantId) {
        Map<String, Integer> defaultWeights = new HashMap<>();
        defaultWeights.put("RATE_LIMITING", 20);
        defaultWeights.put("BRUTE_FORCE", 25);
        defaultWeights.put("CREDENTIAL_STUFFING", 60);
        defaultWeights.put("PASSWORD_SPRAYING", 60);
        defaultWeights.put("API_ABUSE", 30);
        defaultWeights.put("BOT_DETECTION", 30);
        defaultWeights.put("GEOLOCATION", 20);
        defaultWeights.put("IMPOSSIBLE_TRAVEL", 40);
        defaultWeights.put("VPN_DETECTION", 15);
        defaultWeights.put("TOR_EXIT_NODE", 40);
        defaultWeights.put("THREAT_INTELLIGENCE", 50);
        defaultWeights.put("ASN_REPUTATION", 20);
        defaultWeights.put("IP_REPUTATION", 30);
        defaultWeights.put("DEVICE_FINGERPRINT", 15);
        defaultWeights.put("COOKIE_REPUTATION", 10);
        defaultWeights.put("JWT_ABUSE", 50);
        defaultWeights.put("SQL_INJECTION", 50);
        defaultWeights.put("XSS", 40);
        defaultWeights.put("COMMAND_INJECTION", 50);
        defaultWeights.put("PATH_TRAVERSAL", 40);
        defaultWeights.put("MALICIOUS_USER_AGENT", 20);
        defaultWeights.put("FILE_UPLOAD_SCANNER", 50);
        defaultWeights.put("HONEYPOT", 50);
        defaultWeights.put("SESSION_HIJACKING", 40);
        defaultWeights.put("BEHAVIOUR_ANALYTICS", 25);

        for (Map.Entry<String, Integer> entry : defaultWeights.entrySet()) {
            String key = entry.getKey();
            if (configRepository.findByTenantIdAndModuleKey(tenantId, key).isEmpty()) {
                RiskModuleConfig config = new RiskModuleConfig(
                    tenantId,
                    key,
                    true,
                    entry.getValue(),
                    "{}",
                    "Rule configuration for " + key.replace("_", " ")
                );
                configRepository.save(config);
            }
        }
        syncCache(tenantId);
    }

    public void syncCache(String tenantId) {
        List<RiskModuleConfig> configs = configRepository.findAllByTenantId(tenantId);
        for (RiskModuleConfig config : configs) {
            String cacheKey = tenantId + ":" + config.getModuleKey();
            localCache.put(cacheKey, config);
            if (redisAvailable) {
                try {
                    String json = objectMapper.writeValueAsString(config);
                    redisTemplate.opsForValue().set("securegate:tenant:" + tenantId + ":module:" + config.getModuleKey(), json);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    public RiskModuleConfig getModuleConfig(String tenantId, String moduleKey) {
        String cacheKey = tenantId + ":" + moduleKey;
        if (localCache.containsKey(cacheKey)) {
            return localCache.get(cacheKey);
        }
        
        // Fallback to Redis
        if (redisAvailable) {
            try {
                String json = redisTemplate.opsForValue().get("securegate:tenant:" + tenantId + ":module:" + moduleKey);
                if (json != null) {
                    RiskModuleConfig config = objectMapper.readValue(json, RiskModuleConfig.class);
                    localCache.put(cacheKey, config);
                    return config;
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        // Fallback to database
        Optional<RiskModuleConfig> configOpt = configRepository.findByTenantIdAndModuleKey(tenantId, moduleKey);
        if (configOpt.isPresent()) {
            RiskModuleConfig config = configOpt.get();
            localCache.put(cacheKey, config);
            return config;
        }

        return null;
    }

    /**
     * Evaluates total risk score and returns a mitigation decision.
     */
    public RiskDecision evaluate(String tenantId, String ip, VisitorMetadata metadata) {
        // 1. Whitelist Check (always wins)
        if (decisionEngine.isWhitelisted(tenantId, ip)) {
            return new RiskDecision(true, "ALLOW", 0, "IP is whitelisted", Collections.emptyList());
        }

        // 2. Blacklist Check
        if (decisionEngine.isBlacklisted(tenantId, ip)) {
            Optional<BlacklistIP> blacklistEntry = blacklistRepository.findByTenantIdAndIpAddress(tenantId, ip);
            String reason = blacklistEntry.map(BlacklistIP::getReason).orElse("IP is blacklisted");
            return new RiskDecision(false, "BLOCK", 150, "Blacklisted: " + reason, Collections.emptyList());
        }

        int totalScore = 0;
        List<RiskContribution> contributions = new ArrayList<>();
        StringBuilder reasonBuilder = new StringBuilder();

        for (SecurityDetectionModule module : modules) {
            RiskModuleConfig config = getModuleConfig(tenantId, module.getModuleKey());
            if (config != null && config.isEnabled()) {
                try {
                    int score = module.evaluateRisk(tenantId, ip, metadata);
                    if (score > 0) {
                        int finalScore = Math.min(score, config.getRiskWeight());
                        totalScore += finalScore;
                        contributions.add(new RiskContribution(module.getModuleKey(), finalScore, "Threat detected by " + module.getModuleName()));
                        reasonBuilder.append(module.getModuleName()).append(" (").append(finalScore).append("); ");
                    }
                } catch (Exception e) {
                    System.err.println("Error evaluating module " + module.getModuleKey() + ": " + e.getMessage());
                }
            }
        }

        String action = "ALLOW";
        boolean allowed = true;

        if (totalScore >= 120) {
            action = "BLOCK";
            allowed = false;
        } else if (totalScore >= 90) {
            action = "MFA";
        } else if (totalScore >= 60) {
            action = "CAPTCHA";
        } else if (totalScore >= 30) {
            action = "LOG";
        }

        String reason = reasonBuilder.toString().trim();
        if (reason.endsWith(";")) {
            reason = reason.substring(0, reason.length() - 1);
        }
        if (reason.isEmpty()) {
            reason = "No anomaly detected";
        }

        // Write audit log asynchronously to keep request path under 5ms
        logRiskAssessmentAsync(tenantId, ip, totalScore, action, reason, contributions);

        return new RiskDecision(allowed, action, totalScore, reason, contributions);
    }

    @Async
    public void logRiskAssessmentAsync(String tenantId, String ip, int score, String action, String reason, List<RiskContribution> contributions) {
        try {
            String detailsJson = objectMapper.writeValueAsString(contributions);
            RiskAssessment assessment = new RiskAssessment(tenantId, ip, score, action, reason, detailsJson, LocalDateTime.now());
            assessmentRepository.save(assessment);
        } catch (Exception e) {
            System.err.println("Failed to write risk assessment audit log: " + e.getMessage());
        }
    }
}
