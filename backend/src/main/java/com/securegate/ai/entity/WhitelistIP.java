package com.securegate.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "whitelist", uniqueConstraints = {
        @UniqueConstraint(name = "uc_tenant_whitelist_ip", columnNames = {"tenant_id", "ip_address"})
})
public class WhitelistIP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String owner; // e.g., CEO Home, Admin Office

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public WhitelistIP() {
    }

    public WhitelistIP(String tenantId, String ipAddress, String owner, LocalDateTime addedAt) {
        this.tenantId = tenantId;
        this.ipAddress = ipAddress;
        this.owner = owner;
        this.addedAt = addedAt;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
