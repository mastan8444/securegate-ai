package com.securegate.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailAlertService {

    private final JavaMailSender mailSender;
    private final RuleService ruleService;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${securegate.alert.recipient-email}")
    private String recipientEmail;

    public EmailAlertService(JavaMailSender mailSender, RuleService ruleService) {
        this.mailSender = mailSender;
        this.ruleService = ruleService;
    }

    public void sendSecurityAlert(String tenantId, String alertType, String ipAddress, String reason) {
        boolean alertsEnabled = ruleService.getBooleanRule(tenantId, "EMAIL_ALERTS_ENABLED", true);
        if (!alertsEnabled) {
            System.out.println("Email alerts are disabled by policy. Skipping alert for IP " + ipAddress);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(senderEmail);
                message.setTo(recipientEmail);
                message.setSubject("⚠️ SecureGate AI Security Alert: " + alertType);
                message.setText(String.format(
                        "Dear Security Administrator,\n\n" +
                        "A critical security event has been detected and handled by SecureGate AI.\n\n" +
                        "Event Type: %s\n" +
                        "Source IP: %s\n" +
                        "Reason: %s\n" +
                        "Action: IP address has been blocked.\n\n" +
                        "Please check your SecureGate AI admin dashboard for details.\n\n" +
                        "Best regards,\n" +
                        "SecureGate AI Protection Engine",
                        alertType, ipAddress, reason
                ));
                mailSender.send(message);
                System.out.println("Security alert email sent successfully to " + recipientEmail + " for IP " + ipAddress);
            } catch (Exception e) {
                System.err.println("Failed to send security alert email for IP " + ipAddress + ": " + e.getMessage());
            }
        });
    }
}
