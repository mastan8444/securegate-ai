package com.securegate.ai.dto;

import java.util.HashMap;
import java.util.Map;

public class VisitorMetadata {

    private String userAgent;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private String payload;
    private String deviceFingerprint;
    private String cookieReputation;
    private String jwtToken;
    private String username;
    private String password;
    private String failedLoginReason;
    
    // UI behavior data for Bot detection
    private int mouseMovementsCount;
    private int scrollEventsCount;
    private int clicksCount;
    private long keyboardIntervalMs;

    public VisitorMetadata() {
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public String getCookieReputation() {
        return cookieReputation;
    }

    public void setCookieReputation(String cookieReputation) {
        this.cookieReputation = cookieReputation;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFailedLoginReason() {
        return failedLoginReason;
    }

    public void setFailedLoginReason(String failedLoginReason) {
        this.failedLoginReason = failedLoginReason;
    }

    public int getMouseMovementsCount() {
        return mouseMovementsCount;
    }

    public void setMouseMovementsCount(int mouseMovementsCount) {
        this.mouseMovementsCount = mouseMovementsCount;
    }

    public int getScrollEventsCount() {
        return scrollEventsCount;
    }

    public void setScrollEventsCount(int scrollEventsCount) {
        this.scrollEventsCount = scrollEventsCount;
    }

    public int getClicksCount() {
        return clicksCount;
    }

    public void setClicksCount(int clicksCount) {
        this.clicksCount = clicksCount;
    }

    public long getKeyboardIntervalMs() {
        return keyboardIntervalMs;
    }

    public void setKeyboardIntervalMs(long keyboardIntervalMs) {
        this.keyboardIntervalMs = keyboardIntervalMs;
    }
}
