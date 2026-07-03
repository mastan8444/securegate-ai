package com.securegate.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attack_logs")
public class AttackLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String ip;

    @Column(name = "event_type", nullable = false)
    private String eventType; // BRUTE_FORCE, DDOS, RECURRING_ATTACK, GEOLOCATION, CIDR_VIOLATION

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "action_taken", nullable = false)
    private String actionTaken; // BLOCKED, ALLOWED, MONITORED

    @Column(nullable = false)
    private String country; // Country code or name, e.g. IN, RU, CN

    @Column(nullable = false)
    private String reason; // Detailed description of why action was taken

    public AttackLog() {
    }

    public AttackLog(String tenantId, String ip, String eventType, LocalDateTime timestamp, String actionTaken, String country, String reason) {
        this.tenantId = tenantId;
        this.ip = ip;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.actionTaken = actionTaken;
        this.country = country;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
