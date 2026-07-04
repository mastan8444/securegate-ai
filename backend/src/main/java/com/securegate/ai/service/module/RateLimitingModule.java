package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitingModule implements SecurityDetectionModule {

    private final RedisTemplate<String, String> redisTemplate;
    
    // Fallback local memory tracker
    private final ConcurrentHashMap<String, Long> localRateMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStartMap = new ConcurrentHashMap<>();

    public RateLimitingModule(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getModuleKey() {
        return "RATE_LIMITING";
    }

    @Override
    public String getModuleName() {
        return "Sliding Window Rate Limiter";
    }

    @Override
    public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
        String key = "securegate:rate:" + tenantId + ":" + ip;
        long now = System.currentTimeMillis();

        try {
            // Remove points older than 1 minute (60 seconds)
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - 60000);
            // Add current request
            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            // Count total requests in last 60 seconds
            Long count = redisTemplate.opsForZSet().zCard(key);
            
            if (count != null && count > 100) { // Limit: 100 requests/min
                return 100; // Trigger max risk
            }
        } catch (Exception e) {
            // Local fallback
            long windowStart = windowStartMap.computeIfAbsent(key, k -> now);
            if (now - windowStart > 60000) {
                localRateMap.put(key, 1L);
                windowStartMap.put(key, now);
            } else {
                long currentCount = localRateMap.compute(key, (k, val) -> (val == null ? 1L : val + 1));
                if (currentCount > 100) {
                    return 100;
                }
            }
        }
        return 0;
    }
}
