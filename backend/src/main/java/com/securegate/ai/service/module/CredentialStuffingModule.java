package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CredentialStuffingModule implements SecurityDetectionModule {

    private final RedisTemplate<String, String> redisTemplate;
    private final ConcurrentHashMap<String, Set<String>> localUsernamesMap = new ConcurrentHashMap<>();

    public CredentialStuffingModule(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getModuleKey() {
        return "CREDENTIAL_STUFFING";
    }

    @Override
    public String getModuleName() {
        return "Credential Stuffing Detector";
    }

    @Override
    public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
        if (metadata == null || metadata.getUsername() == null) {
            return 0;
        }

        String key = "securegate:stuffing:" + tenantId + ":" + ip;
        String username = metadata.getUsername();

        try {
            redisTemplate.opsForSet().add(key, username);
            Long size = redisTemplate.opsForSet().size(key);
            if (size != null && size > 3) { 
                return 100;
            }
        } catch (Exception e) {
            Set<String> set = localUsernamesMap.computeIfAbsent(key, k -> new HashSet<>());
            synchronized (set) {
                set.add(username);
                if (set.size() > 3) {
                    return 100;
                }
            }
        }
        return 0;
    }
}
