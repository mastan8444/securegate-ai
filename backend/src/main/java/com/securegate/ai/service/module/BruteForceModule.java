package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import com.securegate.ai.service.DecisionEngine;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class BruteForceModule implements SecurityDetectionModule {

    private final DecisionEngine decisionEngine;

    public BruteForceModule(@Lazy DecisionEngine decisionEngine) {
        this.decisionEngine = decisionEngine;
    }

    @Override
    public String getModuleKey() {
        return "BRUTE_FORCE";
    }

    @Override
    public String getModuleName() {
        return "Brute Force Protection";
    }

    @Override
    public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
        int failedAttempts = decisionEngine.getFailedAttempts(tenantId, ip);
        if (failedAttempts > 0) {
            return Math.min(failedAttempts * 20, 100);
        }
        return 0;
    }
}
