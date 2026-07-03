package com.securegate.ai.dto;

public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private String tenantId;

    public LoginResponse(String token, String username, String role, String tenantId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.tenantId = tenantId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
