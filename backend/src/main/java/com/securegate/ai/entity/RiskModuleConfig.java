package com.securegate.ai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "risk_module_configs", indexes = {
    @Index(name = "idx_rmc_tenant_module", columnList = "tenant_id, module_key", unique = true)
})
public class RiskModuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "module_key", nullable = false)
    private String moduleKey;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "risk_weight", nullable = false)
    private int riskWeight;

    @Column(columnDefinition = "TEXT")
    private String thresholds; // JSON-formatted custom threshold strings

    @Column(length = 500)
    private String description;

    public RiskModuleConfig() {
    }

    public RiskModuleConfig(String tenantId, String moduleKey, boolean enabled, int riskWeight, String thresholds, String description) {
        this.tenantId = tenantId;
        this.moduleKey = moduleKey;
        this.enabled = enabled;
        this.riskWeight = riskWeight;
        this.thresholds = thresholds;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRiskWeight() {
        return riskWeight;
    }

    public void setRiskWeight(int riskWeight) {
        this.riskWeight = riskWeight;
    }

    public String getThresholds() {
        return thresholds;
    }

    public void setThresholds(String thresholds) {
        this.thresholds = thresholds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
