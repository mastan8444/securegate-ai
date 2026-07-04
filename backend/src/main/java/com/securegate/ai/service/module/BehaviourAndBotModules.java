package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import com.securegate.ai.service.GeolocationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

public class BehaviourAndBotModules {

    @Service
    public static class BotDetectionModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "BOT_DETECTION";
        }

        @Override
        public String getModuleName() {
            return "Bot Detection & Behavioral Heuristics";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null) return 0;

            // If clicks and scrolls are zero but requests are high, or keystroke velocity is superhuman
            if (metadata.getClicksCount() == 0 && metadata.getScrollEventsCount() == 0 && metadata.getMouseMovementsCount() == 0) {
                // If they submitted a login form or actions without any mouse/scroll events, it is highly likely a script bot!
                if (metadata.getUsername() != null || metadata.getPath() != null) {
                    return 80;
                }
            }
            
            if (metadata.getKeyboardIntervalMs() > 0 && metadata.getKeyboardIntervalMs() < 20) {
                return 100; // Humanly impossible typing speed (e.g. robotic autofill tools)
            }

            return 0;
        }
    }

    @Service
    public static class ImpossibleTravelModule implements SecurityDetectionModule {
        private final GeolocationService geolocationService;
        private final RedisTemplate<String, String> redisTemplate;
        private final ConcurrentHashMap<String, String> localLastLoginMap = new ConcurrentHashMap<>();

        public ImpossibleTravelModule(GeolocationService geolocationService, RedisTemplate<String, String> redisTemplate) {
            this.geolocationService = geolocationService;
            this.redisTemplate = redisTemplate;
        }

        @Override
        public String getModuleKey() {
            return "IMPOSSIBLE_TRAVEL";
        }

        @Override
        public String getModuleName() {
            return "Impossible Travel Guard";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null || metadata.getUsername() == null) return 0;

            String username = metadata.getUsername();
            String currentCountry = geolocationService.resolveCountry(ip);
            String key = "securegate:travel:" + tenantId + ":" + username;
            long now = System.currentTimeMillis();

            String val = null;
            try {
                val = redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                val = localLastLoginMap.get(key);
            }

            if (val != null) {
                String[] parts = val.split(":");
                String lastCountry = parts[0];
                long lastTime = Long.parseLong(parts[1]);

                // If country changed, and it happened in less than 2 hours (impossible travel speed)
                if (!lastCountry.equals(currentCountry) && (now - lastTime < 7200000)) {
                    return 100;
                }
            }

            // Save current login country and timestamp
            String newVal = currentCountry + ":" + now;
            try {
                redisTemplate.opsForValue().set(key, newVal);
            } catch (Exception e) {
                localLastLoginMap.put(key, newVal);
            }

            return 0;
        }
    }

    @Service
    public static class BehaviourAnalyticsModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "BEHAVIOUR_ANALYTICS";
        }

        @Override
        public String getModuleName() {
            return "User Behaviour Analytics (UBA)";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (metadata == null) return 0;

            // Flags anomalies in navigation sequences
            if (metadata.getScrollEventsCount() > 500) {
                return 40; // Scraping behavior anomaly
            }
            return 0;
        }
    }
}
