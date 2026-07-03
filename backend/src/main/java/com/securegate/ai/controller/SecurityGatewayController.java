package com.securegate.ai.controller;

import com.securegate.ai.dto.AccessDecision;
import com.securegate.ai.entity.Tenant;
import com.securegate.ai.repository.TenantRepository;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.RuleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SecurityGatewayController {

    private final DecisionEngine decisionEngine;
    private final TenantRepository tenantRepository;

    public SecurityGatewayController(DecisionEngine decisionEngine, TenantRepository tenantRepository) {
        this.decisionEngine = decisionEngine;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Endpoint for external servers to check if a specific client IP is allowed.
     * URL: GET /api/check/192.168.1.20
     */
    @GetMapping("/check/{ip}")
    public ResponseEntity<AccessDecision> checkAccessForIp(
            @PathVariable String ip,
            @RequestParam(value = "apiKey", required = false) String paramApiKey,
            HttpServletRequest request) {

        // Resolve API key from header or query param
        String apiKey = request.getHeader("X-SecureGate-Key");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = paramApiKey;
        }

        String tenantId = RuleService.DEFAULT_TENANT_ID;

        if (apiKey != null && !apiKey.isBlank()) {
            Optional<Tenant> tenant = tenantRepository.findByApiKey(apiKey);
            if (tenant.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AccessDecision(false, "ERROR", "Invalid API Key"));
            }
            tenantId = tenant.get().getId();
        }

        // Also register HTTP request for DDoS monitoring
        boolean withinDdosLimits = decisionEngine.registerRequest(tenantId, ip);
        if (!withinDdosLimits) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AccessDecision(false, "BLOCKED", "DDoS attack detected: too many requests per minute"));
        }

        AccessDecision decision = decisionEngine.checkAccess(tenantId, ip);
        if (decision.allowed()) {
            return ResponseEntity.ok(decision);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(decision);
        }
    }

    /**
     * Check access for the current caller IP.
     */
    @GetMapping("/check")
    public ResponseEntity<AccessDecision> checkAccessForSelf(
            @RequestParam(value = "apiKey", required = false) String paramApiKey,
            HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        return checkAccessForIp(clientIp, paramApiKey, request);
    }

    /**
     * Simulator: Trigger a mock failed login attempt from a specified IP.
     * POST /api/simulate/failed-login
     * Body: { "ip": "192.168.1.20" }
     */
    @PostMapping("/simulate/failed-login")
    public ResponseEntity<?> simulateFailedLogin(@RequestBody Map<String, String> body) {
        String ip = body.get("ip");
        String apiKey = body.get("apiKey");

        if (ip == null || ip.isBlank()) {
            return ResponseEntity.badRequest().body("ip is required");
        }

        String tenantId = RuleService.DEFAULT_TENANT_ID;
        if (apiKey != null && !apiKey.isBlank()) {
            Optional<Tenant> tenant = tenantRepository.findByApiKey(apiKey);
            if (tenant.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid API Key");
            }
            tenantId = tenant.get().getId();
        }

        decisionEngine.registerFailedLogin(tenantId, ip);
        AccessDecision decision = decisionEngine.checkAccess(tenantId, ip);
        return ResponseEntity.ok(Map.of(
                "ip", ip,
                "message", "Simulated failed login registered",
                "currentStatus", decision.status(),
                "reason", decision.reason()
        ));
    }

    /**
     * Simulator: Trigger a mock DDoS burst from a specified IP (e.g. 130 requests at once).
     * POST /api/simulate/ddos
     * Body: { "ip": "10.20.30.40" }
     */
    @PostMapping("/simulate/ddos")
    public ResponseEntity<?> simulateDdos(@RequestBody Map<String, String> body) {
        String ip = body.get("ip");
        String apiKey = body.get("apiKey");

        if (ip == null || ip.isBlank()) {
            return ResponseEntity.badRequest().body("ip is required");
        }

        String tenantId = RuleService.DEFAULT_TENANT_ID;
        if (apiKey != null && !apiKey.isBlank()) {
            Optional<Tenant> tenant = tenantRepository.findByApiKey(apiKey);
            if (tenant.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid API Key");
            }
            tenantId = tenant.get().getId();
        }

        // Loop to exceed default limit of 120 requests/min
        boolean allowed = true;
        int requestCount = 130;
        for (int i = 0; i < requestCount; i++) {
            allowed = decisionEngine.registerRequest(tenantId, ip);
            if (!allowed) {
                break;
            }
        }

        AccessDecision decision = decisionEngine.checkAccess(tenantId, ip);
        return ResponseEntity.ok(Map.of(
                "ip", ip,
                "requestsSimulated", requestCount,
                "result", allowed ? "ALLOWED" : "BLOCKED",
                "currentStatus", decision.status(),
                "reason", decision.reason()
        ));
    }
}
