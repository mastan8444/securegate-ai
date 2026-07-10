package com.securegate.ai.service;

import com.securegate.ai.dto.AccessDecision;
import com.securegate.ai.entity.AttackLog;
import com.securegate.ai.entity.BlacklistIP;
import com.securegate.ai.entity.WhitelistIP;
import com.securegate.ai.entity.Rule;
import com.securegate.ai.repository.AttackLogRepository;
import com.securegate.ai.repository.BlacklistRepository;
import com.securegate.ai.repository.WhitelistRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DecisionEngine {

    private final BlacklistRepository blacklistRepository;
    private final WhitelistRepository whitelistRepository;
    private final AttackLogRepository attackLogRepository;
    private final RuleService ruleService;
    private final GeolocationService geolocationService;
    private final EmailAlertService emailAlertService;
    private final StringRedisTemplate redisTemplate;

    // High performance O(1) in-memory cache: contains "tenantId:ipAddress"
    private final Set<String> whitelistCache = ConcurrentHashMap.newKeySet();
    private final Set<String> blacklistCache = ConcurrentHashMap.newKeySet();

    // In-memory attack monitoring: key is "tenantId:ipAddress"
    private final Map<String, Integer> failedAttemptsCache = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> requestTimestampsCache = new ConcurrentHashMap<>();

    private boolean redisAvailable = false;

    public DecisionEngine(BlacklistRepository blacklistRepository,
                          WhitelistRepository whitelistRepository,
                          AttackLogRepository attackLogRepository,
                          RuleService ruleService,
                          GeolocationService geolocationService,
                          EmailAlertService emailAlertService,
                          StringRedisTemplate redisTemplate) {
        this.blacklistRepository = blacklistRepository;
        this.whitelistRepository = whitelistRepository;
        this.attackLogRepository = attackLogRepository;
        this.ruleService = ruleService;
        this.geolocationService = geolocationService;
        this.emailAlertService = emailAlertService;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        System.out.println("Initializing Multi-Tenant Decision Engine Cache...");

        // 1. Test Redis availability
        try {
            redisTemplate.opsForValue().set("securegate:ping", "pong");
            String pong = redisTemplate.opsForValue().get("securegate:ping");
            if ("pong".equals(pong)) {
                redisAvailable = true;
                System.out.println("Redis Connection: SUCCESS. Distributed caching is active.");
            }
        } catch (Exception e) {
            redisAvailable = false;
            System.out.println("Redis Connection: OFFLINE. Falling back to local JVM memory caches.");
        }
        
        // 2. Load Whitelist
        List<WhitelistIP> whitelist = whitelistRepository.findAll();
        for (WhitelistIP ip : whitelist) {
            String tenantId = ip.getTenantId();
            String cacheKey = tenantId + ":" + ip.getIpAddress();
            whitelistCache.add(cacheKey);
            
            if (redisAvailable) {
                try {
                    redisTemplate.opsForSet().add("securegate:tenant:" + tenantId + ":whitelist", ip.getIpAddress());
                } catch (Exception e) {
                    // Ignore Redis write fail on startup
                }
            }
        }
        System.out.println("Loaded Whitelist Cache size: " + whitelistCache.size());

        // 3. Load Blacklist (cleaning up expired bans)
        LocalDateTime now = LocalDateTime.now();
        List<BlacklistIP> blacklist = blacklistRepository.findAll();
        for (BlacklistIP ip : blacklist) {
            String tenantId = ip.getTenantId();
            String cacheKey = tenantId + ":" + ip.getIpAddress();
            
            if ("TEMPORARY".equals(ip.getStatus()) && ip.getExpiryTime() != null && ip.getExpiryTime().isBefore(now)) {
                blacklistRepository.delete(ip);
                System.out.println("Cleaned up expired ban for IP: " + ip.getIpAddress() + " of tenant: " + tenantId);
            } else {
                blacklistCache.add(cacheKey);
                if (redisAvailable) {
                    try {
                        redisTemplate.opsForSet().add("securegate:tenant:" + tenantId + ":blacklist", ip.getIpAddress());
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
        System.out.println("Loaded Blacklist Cache size: " + blacklistCache.size());
    }

    /**
     * Checks if the incoming IP is allowed access for a specific tenant.
     */
    public AccessDecision checkAccess(String tenantId, String ip) {
        String country = geolocationService.resolveCountry(ip);

        // 1. Whitelist Check (always wins)
        boolean isWhitelisted = isWhitelisted(tenantId, ip);
        if (isWhitelisted) {
            return new AccessDecision(true, "ALLOWED", "IP is whitelisted");
        }

        // 2. Blacklist Check
        boolean isBlacklisted = isBlacklisted(tenantId, ip);
        if (isBlacklisted) {
            Optional<BlacklistIP> blacklistEntry = blacklistRepository.findByTenantIdAndIpAddress(tenantId, ip);
            if (blacklistEntry.isPresent()) {
                BlacklistIP entry = blacklistEntry.get();
                // Check if expired
                if ("TEMPORARY".equals(entry.getStatus()) && entry.getExpiryTime() != null && entry.getExpiryTime().isBefore(LocalDateTime.now())) {
                    // Remove from cache, Redis, and database
                    if (redisAvailable) {
                        try {
                            redisTemplate.opsForSet().remove("securegate:tenant:" + tenantId + ":blacklist", ip);
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    blacklistCache.remove(tenantId + ":" + ip);
                    blacklistRepository.delete(entry);
                    failedAttemptsCache.remove(tenantId + ":" + ip); // Reset brute force counter
                    System.out.println("Temporary ban expired and cleared for IP: " + ip + " of tenant: " + tenantId);
                } else {
                    return new AccessDecision(false, "BLOCKED", "IP is blacklisted: " + entry.getReason());
                }
            } else {
                // If in cache but not in database, sync
                if (redisAvailable) {
                    try {
                        redisTemplate.opsForSet().remove("securegate:tenant:" + tenantId + ":blacklist", ip);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                blacklistCache.remove(tenantId + ":" + ip);
            }
        }

        // 3. Country-based blocking
        List<String> blockedCountries = ruleService.getListRule(tenantId, "BLOCKED_COUNTRIES");
        if (blockedCountries.contains(country)) {
            String reason = "Access from blocked country: " + country;
            logAttack(tenantId, ip, "GEOLOCATION_VIOLATION", "BLOCKED", country, reason);
            return new AccessDecision(false, "BLOCKED", reason);
        }

        // 4. CIDR range checks
        List<Rule> allRules = ruleService.getAllRules(tenantId);
        for (Rule rule : allRules) {
            if (rule.isEnabled() && rule.getRuleKey().startsWith("CIDR_BLOCK_")) {
                String cidrValue = rule.getRuleValue();
                if (geolocationService.ipMatchesCidr(ip, cidrValue)) {
                    String reason = "IP matches blocked CIDR range: " + cidrValue;
                    logAttack(tenantId, ip, "CIDR_VIOLATION", "BLOCKED", country, reason);
                    return new AccessDecision(false, "BLOCKED", reason);
                }
            }
        }

        return new AccessDecision(true, "ALLOWED", "Normal Access");
    }

    /**
     * Checks if an IP is whitelisted for a tenant.
     */
    public boolean isWhitelisted(String tenantId, String ip) {
        if (redisAvailable) {
            try {
                Boolean member = redisTemplate.opsForSet().isMember("securegate:tenant:" + tenantId + ":whitelist", ip);
                return member != null && member;
            } catch (Exception e) {
                // Fallback to local
            }
        }
        return whitelistCache.contains(tenantId + ":" + ip);
    }

    /**
     * Checks if an IP is blacklisted for a tenant.
     */
    public boolean isBlacklisted(String tenantId, String ip) {
        if (ip == null || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || ip.contains("127.0.0.1") || ip.contains("::1")) {
            return false;
        }
        if (redisAvailable) {
            try {
                Boolean member = redisTemplate.opsForSet().isMember("securegate:tenant:" + tenantId + ":blacklist", ip);
                return member != null && member;
            } catch (Exception e) {
                // Fallback to local
            }
        }
        return blacklistCache.contains(tenantId + ":" + ip);
    }

    /**
     * Registers a failed login attempt for brute force tracking.
     */
    public void registerFailedLogin(String tenantId, String ip) {
        // Whitelisted IPs never get blocked
        if (isWhitelisted(tenantId, ip)) {
            return;
        }

        String cacheKey = tenantId + ":" + ip;
        int failedAttempts = failedAttemptsCache.merge(cacheKey, 1, Integer::sum);
        int tempLimit = ruleService.getIntRule(tenantId, "BRUTE_FORCE_TEMP_LIMIT", 5);
        int permLimit = ruleService.getIntRule(tenantId, "BRUTE_FORCE_PERM_LIMIT", 10);
        String country = geolocationService.resolveCountry(ip);

        System.out.println("Failed login attempt " + failedAttempts + " from IP " + ip + " for tenant: " + tenantId);

        if (failedAttempts >= permLimit) {
            String reason = "Permanent ban: Exceeded limit of " + permLimit + " failed login attempts (Brute Force)";
            blockIP(tenantId, ip, reason, "PERMANENT", null);
            logAttack(tenantId, ip, "BRUTE_FORCE", "BLOCKED", country, reason);
            
            emailAlertService.sendSecurityAlert(tenantId, "Permanent Block (Brute Force)", ip, reason);
        } else if (failedAttempts >= tempLimit) {
            // Check if already temporarily banned
            if (!isBlacklisted(tenantId, ip)) {
                int durationHours = ruleService.getIntRule(tenantId, "TEMP_BAN_DURATION_HOURS", 24);
                LocalDateTime expiryTime = LocalDateTime.now().plusHours(durationHours);
                String reason = "Temporary ban: Exceeded limit of " + tempLimit + " failed login attempts (Brute Force)";
                
                blockIP(tenantId, ip, reason, "TEMPORARY", expiryTime);
                logAttack(tenantId, ip, "BRUTE_FORCE", "BLOCKED", country, reason);
                
                emailAlertService.sendSecurityAlert(tenantId, "Temporary Ban (Brute Force)", ip, reason + ". Expire time: " + expiryTime);
            }
        }
    }

    /**
     * Resets failed login attempts.
     */
    public void resetFailedLogins(String tenantId, String ip) {
        failedAttemptsCache.remove(tenantId + ":" + ip);
    }

    public int getFailedAttempts(String tenantId, String ip) {
        return failedAttemptsCache.getOrDefault(tenantId + ":" + ip, 0);
    }

    /**
     * Registers an HTTP request to check and prevent DDoS attacks.
     * Returns true if allowed, false if blocked due to DDoS detection.
     */
    public boolean registerRequest(String tenantId, String ip) {
        if (isWhitelisted(tenantId, ip)) {
            return true;
        }

        long nowMs = System.currentTimeMillis();
        String cacheKey = tenantId + ":" + ip;
        List<Long> timestamps = requestTimestampsCache.computeIfAbsent(cacheKey, k -> Collections.synchronizedList(new ArrayList<>()));

        // Remove old timestamps (> 60 seconds ago)
        synchronized (timestamps) {
            timestamps.removeIf(t -> nowMs - t > 60000);
            timestamps.add(nowMs);
            
            int threshold = ruleService.getIntRule(tenantId, "DDOS_THRESHOLD_MIN", 120);
            if (timestamps.size() > threshold) {
                if (!isBlacklisted(tenantId, ip)) {
                    String country = geolocationService.resolveCountry(ip);
                    int durationHours = ruleService.getIntRule(tenantId, "TEMP_BAN_DURATION_HOURS", 24);
                    LocalDateTime expiryTime = LocalDateTime.now().plusHours(durationHours);
                    String reason = "DDoS Attack detected: " + timestamps.size() + " requests in a single minute (Threshold: " + threshold + ")";
                    
                    blockIP(tenantId, ip, reason, "TEMPORARY", expiryTime);
                    logAttack(tenantId, ip, "DDOS", "BLOCKED", country, reason);
                    
                    emailAlertService.sendSecurityAlert(tenantId, "Temporary Ban (DDoS Attack)", ip, reason + ". Expire time: " + expiryTime);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Core method to block an IP address.
     */
     public void blockIP(String tenantId, String ip, String reason, String status, LocalDateTime expiryTime) {
        if (redisAvailable) {
            try {
                redisTemplate.opsForSet().add("securegate:tenant:" + tenantId + ":blacklist", ip);
            } catch (Exception e) {
                // Ignore
            }
        }
        blacklistCache.add(tenantId + ":" + ip);
        
        // Persist to DB
        BlacklistIP blacklistIP = blacklistRepository.findByTenantIdAndIpAddress(tenantId, ip)
                .orElse(new BlacklistIP());
        
        blacklistIP.setTenantId(tenantId);
        blacklistIP.setIpAddress(ip);
        blacklistIP.setReason(reason);
        blacklistIP.setBlockedAt(LocalDateTime.now());
        blacklistIP.setStatus(status);
        blacklistIP.setExpiryTime(expiryTime);
        
        blacklistRepository.save(blacklistIP);
        System.out.println("IP " + ip + " added to blacklist for tenant " + tenantId + " with status " + status);
    }

    /**
     * Core method to unblock an IP.
     */
    public void unblockIP(String tenantId, String ip) {
        if (redisAvailable) {
            try {
                redisTemplate.opsForSet().remove("securegate:tenant:" + tenantId + ":blacklist", ip);
            } catch (Exception e) {
                // Ignore
            }
        }
        blacklistCache.remove(tenantId + ":" + ip);
        blacklistRepository.findByTenantIdAndIpAddress(tenantId, ip).ifPresent(blacklistRepository::delete);
        failedAttemptsCache.remove(tenantId + ":" + ip);
        requestTimestampsCache.remove(tenantId + ":" + ip);
        System.out.println("IP " + ip + " unblocked successfully for tenant: " + tenantId);
    }

    /**
     * Core method to add to whitelist.
     */
    public void whitelistIP(String tenantId, String ip, String owner) {
        if (redisAvailable) {
            try {
                redisTemplate.opsForSet().add("securegate:tenant:" + tenantId + ":whitelist", ip);
                redisTemplate.opsForSet().remove("securegate:tenant:" + tenantId + ":blacklist", ip);
            } catch (Exception e) {
                // Ignore
            }
        }
        whitelistCache.add(tenantId + ":" + ip);
        
        // If it was blacklisted, remove from blacklist
        if (isBlacklisted(tenantId, ip)) {
            unblockIP(tenantId, ip);
        }
        
        WhitelistIP whitelistIP = whitelistRepository.findByTenantIdAndIpAddress(tenantId, ip)
                .orElse(new WhitelistIP());
        
        whitelistIP.setTenantId(tenantId);
        whitelistIP.setIpAddress(ip);
        whitelistIP.setOwner(owner);
        whitelistIP.setAddedAt(LocalDateTime.now());
        
        whitelistRepository.save(whitelistIP);
        System.out.println("IP " + ip + " added to whitelist for tenant " + tenantId + " owned by " + owner);
    }

    /**
     * Core method to remove from whitelist.
     */
    public void removeFromWhitelist(String tenantId, String ip) {
        if (redisAvailable) {
            try {
                redisTemplate.opsForSet().remove("securegate:tenant:" + tenantId + ":whitelist", ip);
            } catch (Exception e) {
                // Ignore
            }
        }
        whitelistCache.remove(tenantId + ":" + ip);
        whitelistRepository.findByTenantIdAndIpAddress(tenantId, ip).ifPresent(whitelistRepository::delete);
        System.out.println("IP " + ip + " removed from whitelist for tenant: " + tenantId);
    }

    public AccessDecision evaluateVisitorThreat(String tenantId, String ip, String userAgent, String path) {
        // 1. Whitelist Check (always wins)
        if (isWhitelisted(tenantId, ip)) {
            return new AccessDecision(true, "ALLOWED", "IP is whitelisted");
        }
        
        // 2. Blacklist Check
        if (isBlacklisted(tenantId, ip)) {
            return checkAccess(tenantId, ip);
        }

        String country = geolocationService.resolveCountry(ip);

        // 3. User-Agent Scanner Detection
        if (ruleService.getBooleanRule(tenantId, "SUSPICIOUS_UA_BLOCKING", true) && userAgent != null) {
            String uaLower = userAgent.toLowerCase();
            if (uaLower.contains("sqlmap") || uaLower.contains("nmap") || uaLower.contains("nikto") || uaLower.contains("dirbuster") || uaLower.contains("w3af")) {
                String reason = "Suspicious User-Agent detected: " + userAgent;
                blockIP(tenantId, ip, reason, "PERMANENT", null);
                logAttack(tenantId, ip, "SCANNER_DETECTION", "BLOCKED", country, reason);
                return new AccessDecision(false, "BLOCKED", reason);
            }
        }

        // 4. Suspicious Admin Path Scanning Detection
        if (ruleService.getBooleanRule(tenantId, "SUSPICIOUS_PATH_BLOCKING", true) && path != null) {
            String pathLower = path.toLowerCase();
            if (pathLower.contains("/wp-admin") || pathLower.contains("/.git") || pathLower.contains("/actuator") || pathLower.contains("/etc/passwd") || pathLower.contains("/config.json")) {
                String reason = "Suspicious path access attempt: " + path;
                blockIP(tenantId, ip, reason, "PERMANENT", null);
                logAttack(tenantId, ip, "PATH_SCAN_DETECTION", "BLOCKED", country, reason);
                return new AccessDecision(false, "BLOCKED", reason);
            }
        }

        // 5. Default rules
        return checkAccess(tenantId, ip);
    }

    private void logAttack(String tenantId, String ip, String eventType, String actionTaken, String country, String reason) {
        AttackLog log = new AttackLog(tenantId, ip, eventType, LocalDateTime.now(), actionTaken, country, reason);
        attackLogRepository.save(log);
    }

    // Cache getters for specific tenant (filters from the global set)
    public Set<String> getWhitelistCacheForTenant(String tenantId) {
        Set<String> tenantSet = new HashSet<>();
        for (String entry : whitelistCache) {
            if (entry.startsWith(tenantId + ":")) {
                tenantSet.add(entry.substring(tenantId.length() + 1));
            }
        }
        return tenantSet;
    }

    public Set<String> getBlacklistCacheForTenant(String tenantId) {
        Set<String> tenantSet = new HashSet<>();
        for (String entry : blacklistCache) {
            if (entry.startsWith(tenantId + ":")) {
                tenantSet.add(entry.substring(tenantId.length() + 1));
            }
        }
        return tenantSet;
    }

    public Set<String> getWhitelistCache() {
        return whitelistCache;
    }

    public Set<String> getBlacklistCache() {
        return blacklistCache;
    }
}
