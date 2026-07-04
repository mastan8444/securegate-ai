package com.securegate.ai.dto;

public class RiskContribution {

    private String moduleKey;
    private int score;
    private String reason;

    public RiskContribution() {
    }

    public RiskContribution(String moduleKey, int score, String reason) {
        this.moduleKey = moduleKey;
        this.score = score;
        this.reason = reason;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
