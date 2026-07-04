package com.securegate.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tor_exit_nodes", indexes = {
    @Index(name = "idx_tor_ip", columnList = "ip_address", unique = true)
})
public class TorExitNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TorExitNode() {
    }

    public TorExitNode(String ipAddress, LocalDateTime updatedAt) {
        this.ipAddress = ipAddress;
        this.updatedAt = updatedAt;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
