package com.securegate.ai.repository;

import com.securegate.ai.entity.RiskModuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskModuleConfigRepository extends JpaRepository<RiskModuleConfig, Long> {
    List<RiskModuleConfig> findAllByTenantId(String tenantId);
    Optional<RiskModuleConfig> findByTenantIdAndModuleKey(String tenantId, String moduleKey);
}
