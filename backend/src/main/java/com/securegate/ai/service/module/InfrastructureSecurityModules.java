package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

public class InfrastructureSecurityModules {

    @Service
    public static class ApiAbuseModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "API_ABUSE";
        }

        @Override
        public String getModuleName() {
            return "API Abuse Monitor";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getPath() == null) return 0;
            String path = metadata.getPath().toLowerCase();
            
            // Flag high risk if requesting sensitive action gateways abnormally
            if (path.contains("/login") || path.contains("/register") || path.contains("/forgot-password") || path.contains("/checkout") || path.contains("/payment")) {
                // Return mild score contribution that accumulates if done in high volumes
                return 20; 
            }
            return 0;
        }
    }

    @Service
    public static class HoneypotModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "HONEYPOT";
        }

        @Override
        public String getModuleName() {
            return "Honeypot Trap Shield";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getPath() == null) return 0;
            String path = metadata.getPath().toLowerCase();
            
            if (path.contains("/.env") || path.contains("/wp-admin") || path.contains("/phpmyadmin") || path.contains("/admin/config") || path.contains("/actuator")) {
                return 100; // Zero tolerance for honeypot hits
            }
            return 0;
        }
    }

    @Service
    public static class MaliciousUserAgentModule implements SecurityDetectionModule {
        private static final Pattern AGENT_PATTERN = Pattern.compile(
            "(?i)(sqlmap|nikto|nmap|masscan|acunetix|dirbuster|python-requests|curl|wget|go-http-client)"
        );

        @Override
        public String getModuleKey() {
            return "MALICIOUS_USER_AGENT";
        }

        @Override
        public String getModuleName() {
            return "Malicious User Agent Filter";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getUserAgent() == null) return 0;
            String ua = metadata.getUserAgent();
            
            if (AGENT_PATTERN.matcher(ua).find()) {
                return 80;
            }
            return 0;
        }
    }

    @Service
    public static class FileUploadScannerModule implements SecurityDetectionModule {
        private static final Pattern EXECUTABLE_PATTERN = Pattern.compile(
            "(?i)\\.(php|jsp|asp|exe|sh|bat|pl|py|cgi|jar|war|dll)$"
        );

        @Override
        public String getModuleKey() {
            return "FILE_UPLOAD_SCANNER";
        }

        @Override
        public String getModuleName() {
            return "File Upload Extension Scanner";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getPath() == null) return 0;
            String path = metadata.getPath().toLowerCase();
            
            // Check double extension trick (e.g. image.jpg.php)
            if (path.contains(".jpg.") || path.contains(".png.") || path.contains(".gif.")) {
                if (EXECUTABLE_PATTERN.matcher(path).find()) {
                    return 100;
                }
            }
            
            if (EXECUTABLE_PATTERN.matcher(path).find()) {
                return 100;
            }
            return 0;
        }
    }
}
