package com.securegate.ai.repository;

import com.securegate.ai.entity.BlacklistIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistIP, Long> {
    Optional<BlacklistIP> findByTenantIdAndIpAddress(String tenantId, String ipAddress);
    List<BlacklistIP> findByTenantIdAndStatusAndExpiryTimeBefore(String tenantId, String status, LocalDateTime expiryTimeBefore);
    List<BlacklistIP> findAllByTenantId(String tenantId);
    long countByTenantId(String tenantId);
}
