package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

public class IdentityAndAccessModules {

    @Service
    public static class JwtAbuseModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "JWT_ABUSE";
        }

        @Override
        public String getModuleName() {
            return "JWT Abuse & Signature Guard";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getJwtToken() == null) return 0;
            String jwt = metadata.getJwtToken();

            // Simple structural verify of JWT format
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return 100; // Malformed JWT structures
            }
            return 0;
        }
    }

    @Service
    public static class SessionHijackingModule implements SecurityDetectionModule {
        private final RedisTemplate<String, String> redisTemplate;
        private final ConcurrentHashMap<String, String> localSessionTracker = new ConcurrentHashMap<>();

        public SessionHijackingModule(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public String getModuleKey() {
            return "SESSION_HIJACKING";
        }

        @Override
        public String getModuleName() {
            return "Session Hijacking Detector";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getJwtToken() == null) return 0;
            String token = metadata.getJwtToken();
            String key = "securegate:session:" + tenantId + ":" + token.hashCode();

            try {
                String existingIp = redisTemplate.opsForValue().get(key);
                if (existingIp == null) {
                    redisTemplate.opsForValue().set(key, ip);
                } else if (!existingIp.equals(ip)) {
                    return 80; // IP changed during active JWT session
                }
            } catch (Exception e) {
                String existingIp = localSessionTracker.putIfAbsent(key, ip);
                if (existingIp != null && !existingIp.equals(ip)) {
                    return 80;
                }
            }
            return 0;
        }
    }

    @Service
    public static class DeviceFingerprintModule implements SecurityDetectionModule {
        private final RedisTemplate<String, String> redisTemplate;
        private final ConcurrentHashMap<String, String> localFingerprintTracker = new ConcurrentHashMap<>();

        public DeviceFingerprintModule(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public String getModuleKey() {
            return "DEVICE_FINGERPRINT";
        }

        @Override
        public String getModuleName() {
            return "Device Fingerprint Analyser";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getDeviceFingerprint() == null || metadata.getJwtToken() == null) return 0;
            
            String token = metadata.getJwtToken();
            String fingerprint = metadata.getDeviceFingerprint();
            String key = "securegate:fingerprint:" + tenantId + ":" + token.hashCode();

            try {
                String existingFingerprint = redisTemplate.opsForValue().get(key);
                if (existingFingerprint == null) {
                    redisTemplate.opsForValue().set(key, fingerprint);
                } else if (!existingFingerprint.equals(fingerprint)) {
                    return 100; // Device hardware fingerprint changed mid-session
                }
            } catch (Exception e) {
                String existingFingerprint = localFingerprintTracker.putIfAbsent(key, fingerprint);
                if (existingFingerprint != null && !existingFingerprint.equals(fingerprint)) {
                    return 100;
                }
            }
            return 0;
        }
    }

    @Service
    public static class CookieReputationModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "COOKIE_REPUTATION";
        }

        @Override
        public String getModuleName() {
            return "Cookie Reputation Monitor";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getCookieReputation() == null) return 0;
            String cookie = metadata.getCookieReputation();
            
            if (cookie.equalsIgnoreCase("malformed") || cookie.equalsIgnoreCase("manipulated")) {
                return 50;
            }
            return 0;
        }
    }
}
