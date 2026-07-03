package com.securegate.ai.security;

import com.securegate.ai.dto.AccessDecision;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.RuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IpFilter extends OncePerRequestFilter {

    private final DecisionEngine decisionEngine;
    private final ObjectMapper objectMapper;

    public IpFilter(DecisionEngine decisionEngine, ObjectMapper objectMapper) {
        this.decisionEngine = decisionEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String ipAddress = request.getRemoteAddr();

        // 1. DDoS sliding window check on our own endpoints (self-defense)
        boolean withinDdosLimits = decisionEngine.registerRequest(RuleService.DEFAULT_TENANT_ID, ipAddress);
        if (!withinDdosLimits) {
            sendBlockedResponse(response, "DDoS attack detected: too many requests per minute");
            return;
        }

        // 2. Blacklist / Geolocation / CIDR check (self-defense)
        AccessDecision decision = decisionEngine.checkAccess(RuleService.DEFAULT_TENANT_ID, ipAddress);
        if (!decision.allowed()) {
            sendBlockedResponse(response, decision.reason());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendBlockedResponse(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        AccessDecision accessDecision = new AccessDecision(false, "BLOCKED", reason);
        String jsonResponse = objectMapper.writeValueAsString(accessDecision);
        response.getWriter().write(jsonResponse);
    }
}
