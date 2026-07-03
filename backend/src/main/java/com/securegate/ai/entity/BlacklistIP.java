package com.securegate.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist", uniqueConstraints = {
        @UniqueConstraint(name = "uc_tenant_ip", columnNames = {"tenant_id", "ip_address"})
})
public class BlacklistIP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String reason;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    @Column(nullable = false)
    private String status; // TEMPORARY, PERMANENT

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime; // Null for PERMANENT

    public BlacklistIP() {
    }

    public BlacklistIP(String tenantId, String ipAddress, String reason, LocalDateTime blockedAt, String status, LocalDateTime expiryTime) {
        this.tenantId = tenantId;
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.blockedAt = blockedAt;
        this.status = status;
        this.expiryTime = expiryTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
