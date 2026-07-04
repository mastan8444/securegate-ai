package com.securegate.ai.repository;

import com.securegate.ai.entity.RiskAssessment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findAllByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.tenantId = ?1 ORDER BY ra.createdAt DESC")
    List<RiskAssessment> findLatestAssessments(String tenantId, Pageable pageable);
}
