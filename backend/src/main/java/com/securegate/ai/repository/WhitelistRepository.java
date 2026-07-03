package com.securegate.ai.repository;

import com.securegate.ai.entity.WhitelistIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhitelistRepository extends JpaRepository<WhitelistIP, Long> {
    Optional<WhitelistIP> findByTenantIdAndIpAddress(String tenantId, String ipAddress);
    List<WhitelistIP> findAllByTenantId(String tenantId);
    long countByTenantId(String tenantId);
}
