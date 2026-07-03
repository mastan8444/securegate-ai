package com.securegate.ai.repository;

import com.securegate.ai.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    Optional<Rule> findByTenantIdAndRuleKey(String tenantId, String ruleKey);
    List<Rule> findAllByTenantId(String tenantId);
}
