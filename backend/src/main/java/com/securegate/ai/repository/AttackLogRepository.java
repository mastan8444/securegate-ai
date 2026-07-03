package com.securegate.ai.repository;

import com.securegate.ai.entity.AttackLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttackLogRepository extends JpaRepository<AttackLog, Long> {
    
    List<AttackLog> findAllByTenantIdOrderByTimestampDesc(String tenantId, Pageable pageable);
    
    long countByTenantIdAndActionTaken(String tenantId, String actionTaken);

    long countByTenantId(String tenantId);

    @Query("SELECT a.ip, COUNT(a.ip) as cnt FROM AttackLog a WHERE a.tenantId = :tenantId GROUP BY a.ip ORDER BY cnt DESC")
    List<Object[]> findTopAttackers(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT DATE(a.timestamp), COUNT(a.id) FROM AttackLog a WHERE a.tenantId = :tenantId GROUP BY DATE(a.timestamp) ORDER BY DATE(a.timestamp) ASC")
    List<Object[]> findAttacksPerDay(@Param("tenantId") String tenantId);
}
