package com.securegate.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_api_key", columnList = "apiKey", unique = true)
})
public class Tenant {

    @Id
    private String id; // UUID or custom unique slug

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private String planType; // FREE, ENTERPRISE, etc.

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Tenant() {}

    public Tenant(String id, String name, String apiKey, String planType) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
        this.planType = planType;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
