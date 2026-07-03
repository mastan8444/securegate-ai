package com.securegate.ai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rules", uniqueConstraints = {
        @UniqueConstraint(name = "uc_tenant_rule_key", columnNames = {"tenant_id", "rule_key"})
})
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "rule_key", nullable = false)
    private String ruleKey; // e.g. BRUTE_FORCE_LIMIT, DDOS_THRESHOLD_MIN, BLOCKED_COUNTRIES, CIDR_RANGES, EMAIL_ALERTS_ENABLED

    @Column(name = "rule_value", nullable = false)
    private String ruleValue; // e.g. "5", "100", "CN,RU,KP", "192.168.1.0/24", "true"

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean enabled;

    public Rule() {
    }

    public Rule(String tenantId, String ruleKey, String ruleValue, String description, boolean enabled) {
        this.tenantId = tenantId;
        this.ruleKey = ruleKey;
        this.ruleValue = ruleValue;
        this.description = description;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
