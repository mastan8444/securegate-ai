package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordSprayingModule implements SecurityDetectionModule {

    private final RedisTemplate<String, String> redisTemplate;
    private final ConcurrentHashMap<String, Set<String>> localPasswordsMap = new ConcurrentHashMap<>();

    public PasswordSprayingModule(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getModuleKey() {
        return "PASSWORD_SPRAYING";
    }

    @Override
    public String getModuleName() {
        return "Password Spraying Detector";
    }

    @Override
    public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
        if (metadata == null || metadata.getPassword() == null || metadata.getUsername() == null) {
            return 0;
        }

        String password = metadata.getPassword();
        String username = metadata.getUsername();
        String key = "securegate:spraying:" + tenantId + ":" + password;

        try {
            redisTemplate.opsForSet().add(key, username);
            Long size = redisTemplate.opsForSet().size(key);
            if (size != null && size > 5) { 
                return 100;
            }
        } catch (Exception e) {
            Set<String> set = localPasswordsMap.computeIfAbsent(key, k -> new HashSet<>());
            synchronized (set) {
                set.add(username);
                if (set.size() > 5) {
                    return 100;
                }
            }
        }
        return 0;
    }
}
