package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;

public interface SecurityDetectionModule {
    String getModuleKey();
    String getModuleName();
    int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata);
}
