package com.securegate.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/api/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "platform", "SecureGate AI Smart Security Gateway",
                "status", "UP & ACTIVE",
                "message", "Gateway protection engine is fully operational.",
                "dashboardUrl", "http://localhost:8080",
                "securityCheckEndpoint", "GET /api/check/{ip}"
        ));
    }
}
