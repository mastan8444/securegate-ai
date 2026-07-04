package com.securegate.ai.service;

import com.securegate.ai.entity.Rule;
import com.securegate.ai.repository.RuleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RuleService {

    public static final String DEFAULT_TENANT_ID = "system";
    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @PostConstruct
    public void initRules() {
        seedTenantRules(DEFAULT_TENANT_ID);
    }

    public void seedTenantRules(String tenantId) {
        seedRule(tenantId, "BRUTE_FORCE_TEMP_LIMIT", "5", "Failed login attempts before a temporary ban", true);
        seedRule(tenantId, "BRUTE_FORCE_PERM_LIMIT", "10", "Failed login attempts before a permanent blacklist", true);
        seedRule(tenantId, "DDOS_THRESHOLD_MIN", "120", "Max requests allowed per minute from a single IP", true);
        seedRule(tenantId, "BLOCKED_COUNTRIES", "CN,RU,KP", "Comma-separated list of blocked ISO country codes (e.g. CN,RU,KP)", false);
        seedRule(tenantId, "EMAIL_ALERTS_ENABLED", "true", "Enable Gmail notifications for security blocks", true);
        seedRule(tenantId, "TEMP_BAN_DURATION_HOURS", "24", "Duration of temporary bans in hours", true);
        seedRule(tenantId, "SUSPICIOUS_PATH_BLOCKING", "true", "Automatically ban IPs scanning sensitive system folders (e.g. /wp-admin, /.git, /actuator)", true);
        seedRule(tenantId, "SUSPICIOUS_UA_BLOCKING", "true", "Automatically ban IPs sending requests using scanner User-Agents (e.g. sqlmap, nmap, nikto)", true);
        seedRule(tenantId, "SCAN_ERROR_LIMIT_MIN", "15", "Maximum 404/403 errors allowed per minute from a single IP before blocking", true);
    }

    private void seedRule(String tenantId, String key, String defaultValue, String description, boolean enabled) {
        if (ruleRepository.findByTenantIdAndRuleKey(tenantId, key).isEmpty()) {
            ruleRepository.save(new Rule(tenantId, key, defaultValue, description, enabled));
        }
    }

    public List<Rule> getAllRules(String tenantId) {
        return ruleRepository.findAllByTenantId(tenantId);
    }

    public Optional<Rule> getRule(String tenantId, String key) {
        return ruleRepository.findByTenantIdAndRuleKey(tenantId, key);
    }

    public Rule updateRule(String tenantId, String key, String value, boolean enabled) {
        Rule rule = ruleRepository.findByTenantIdAndRuleKey(tenantId, key)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with key: " + key + " for tenant: " + tenantId));
        rule.setRuleValue(value);
        rule.setEnabled(enabled);
        return ruleRepository.save(rule);
    }

    public int getIntRule(String tenantId, String key, int defaultValue) {
        return ruleRepository.findByTenantIdAndRuleKey(tenantId, key)
                .filter(Rule::isEnabled)
                .map(r -> {
                    try {
                        return Integer.parseInt(r.getRuleValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    public boolean getBooleanRule(String tenantId, String key, boolean defaultValue) {
        return ruleRepository.findByTenantIdAndRuleKey(tenantId, key)
                .filter(Rule::isEnabled)
                .map(r -> Boolean.parseBoolean(r.getRuleValue()))
                .orElse(defaultValue);
    }

    public List<String> getListRule(String tenantId, String key) {
        return ruleRepository.findByTenantIdAndRuleKey(tenantId, key)
                .filter(Rule::isEnabled)
                .map(r -> Arrays.asList(r.getRuleValue().split(",")))
                .orElse(Collections.emptyList());
    }
}
