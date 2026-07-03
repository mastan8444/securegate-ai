package com.securegate.ai.dto;

public record AccessDecision(boolean allowed, String status, String reason) {
}
