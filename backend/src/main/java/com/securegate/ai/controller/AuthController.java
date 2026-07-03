package com.securegate.ai.controller;

import com.securegate.ai.dto.LoginRequest;
import com.securegate.ai.dto.LoginResponse;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.UserRepository;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.RuleService;
import com.securegate.ai.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final DecisionEngine decisionEngine;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          DecisionEngine decisionEngine,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.decisionEngine = decisionEngine;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        try {
            // First check if the IP is currently blocked (using the gateway self-defense system tenant)
            var decision = decisionEngine.checkAccess(RuleService.DEFAULT_TENANT_ID, clientIp);
            if (!decision.allowed()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access Blocked: Your IP is currently blacklisted. Reason: " + decision.reason());
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            // Reset attempts on success
            decisionEngine.resetFailedLogins(RuleService.DEFAULT_TENANT_ID, clientIp);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            // Retrieve the tenantId for this user
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User entity not found"));
            String tenantId = user.getTenantId() != null ? user.getTenantId() : RuleService.DEFAULT_TENANT_ID;

            return ResponseEntity.ok(new LoginResponse(jwt, userDetails.getUsername(), role, tenantId));

        } catch (BadCredentialsException ex) {
            // Register failed login under system tenant to monitor login panel brute forcing
            decisionEngine.registerFailedLogin(RuleService.DEFAULT_TENANT_ID, clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid username or password.");
        }
    }
}
