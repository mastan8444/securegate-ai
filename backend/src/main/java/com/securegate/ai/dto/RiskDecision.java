package com.securegate.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class RiskDecision {

    private boolean allowed;
    private String action; // ALLOW, LOG, CAPTCHA, MFA, BLOCK
    private int score;
    private String reason;
    private List<RiskContribution> contributions = new ArrayList<>();

    public RiskDecision() {
    }

    public RiskDecision(boolean allowed, String action, int score, String reason, List<RiskContribution> contributions) {
        this.allowed = allowed;
        this.action = action;
        this.score = score;
        this.reason = reason;
        this.contributions = contributions;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public List<RiskContribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<RiskContribution> contributions) {
        this.contributions = contributions;
    }
}
