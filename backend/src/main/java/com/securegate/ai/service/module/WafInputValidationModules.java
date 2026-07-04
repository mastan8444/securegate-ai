package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

public class WafInputValidationModules {

    @Service
    public static class SqlInjectionModule implements SecurityDetectionModule {
        private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)(union\\s+select|select\\s+.*\\s+from|\\bdrop\\b|\\bdelete\\b|\\binsert\\b|or\\s+\\d+=\\d+|\\bsleep\\(\\d+\\)|\\bbenchmark\\b|information_schema)"
        );

        @Override
        public String getModuleKey() {
            return "SQL_INJECTION";
        }

        @Override
        public String getModuleName() {
            return "SQL Injection Shield";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null) return 0;
            String inspect = (metadata.getPath() != null ? metadata.getPath() : "") + " " +
                             (metadata.getPayload() != null ? metadata.getPayload() : "");
            
            if (SQL_PATTERN.matcher(inspect).find()) {
                return 100;
            }
            return 0;
        }
    }

    @Service
    public static class XssModule implements SecurityDetectionModule {
        private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|javascript:|onerror=|onload=|onclick=|alert\\(|confirm\\(|prompt\\(|<img\\s+src)"
        );

        @Override
        public String getModuleKey() {
            return "XSS";
        }

        @Override
        public String getModuleName() {
            return "Cross-Site Scripting Protection";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null) return 0;
            String inspect = (metadata.getPath() != null ? metadata.getPath() : "") + " " +
                             (metadata.getPayload() != null ? metadata.getPayload() : "");
            
            if (XSS_PATTERN.matcher(inspect).find()) {
                return 100;
            }
            return 0;
        }
    }

    @Service
    public static class CommandInjectionModule implements SecurityDetectionModule {
        private static final Pattern CMD_PATTERN = Pattern.compile(
            "(?i)(\\bcurl\\b|\\bwget\\b|\\bbash\\b|\\bpowershell\\b|\\bcmd\\.exe\\b|\\|\\||&&|;)"
        );

        @Override
        public String getModuleKey() {
            return "COMMAND_INJECTION";
        }

        @Override
        public String getModuleName() {
            return "Command Injection Protection";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null) return 0;
            String inspect = (metadata.getPath() != null ? metadata.getPath() : "") + " " +
                             (metadata.getPayload() != null ? metadata.getPayload() : "");
            
            // Filter common safe path semicolons/parameters before checking cmd injection
            if (inspect.contains(";") || inspect.contains("&&") || inspect.contains("||")) {
                if (inspect.matches(".*(\\bcurl\\b|\\bwget\\b|\\bbash\\b|\\bpowershell\\b).*")) {
                    return 100;
                }
            }
            if (CMD_PATTERN.matcher(inspect).find()) {
                return 50; // Moderate risk flag for command keywords
            }
            return 0;
        }
    }

    @Service
    public static class PathTraversalModule implements SecurityDetectionModule {
        private static final Pattern TRAVERSAL_PATTERN = Pattern.compile(
            "(?i)(\\.\\./|\\.\\.\\\\|etc/passwd|boot\\.ini|/win\\.ini|/etc/hosts)"
        );

        @Override
        public String getModuleKey() {
            return "PATH_TRAVERSAL";
        }

        @Override
        public String getModuleName() {
            return "Path Traversal Detector";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null) return 0;
            String inspect = (metadata.getPath() != null ? metadata.getPath() : "") + " " +
                             (metadata.getPayload() != null ? metadata.getPayload() : "");
            
            if (TRAVERSAL_PATTERN.matcher(inspect).find()) {
                return 100;
            }
            return 0;
        }
    }
}
