package com.securegate.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments", indexes = {
    @Index(name = "idx_ra_tenant", columnList = "tenant_id"),
    @Index(name = "idx_ra_created", columnList = "created_at"),
    @Index(name = "idx_ra_ip", columnList = "ip_address")
})
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "action_taken", nullable = false)
    private String actionTaken;

    @Column(length = 500)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String details; // JSON representation of module score contributions

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RiskAssessment() {
    }

    public RiskAssessment(String tenantId, String ipAddress, int riskScore, String actionTaken, String reason, String details, LocalDateTime createdAt) {
        this.tenantId = tenantId;
        this.ipAddress = ipAddress;
        this.riskScore = riskScore;
        this.actionTaken = actionTaken;
        this.reason = reason;
        this.details = details;
        this.createdAt = createdAt;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
