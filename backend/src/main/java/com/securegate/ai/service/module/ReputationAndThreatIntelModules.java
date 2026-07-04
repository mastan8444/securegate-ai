package com.securegate.ai.service.module;

import com.securegate.ai.dto.VisitorMetadata;
import com.securegate.ai.repository.TorExitNodeRepository;
import com.securegate.ai.repository.AttackLogRepository;
import com.securegate.ai.service.GeolocationService;
import com.securegate.ai.service.RuleService;
import org.springframework.stereotype.Service;

import java.util.List;

public class ReputationAndThreatIntelModules {

    @Service
    public static class GeolocationModule implements SecurityDetectionModule {
        private final GeolocationService geolocationService;
        private final RuleService ruleService;

        public GeolocationModule(GeolocationService geolocationService, RuleService ruleService) {
            this.geolocationService = geolocationService;
            this.ruleService = ruleService;
        }

        @Override
        public String getModuleKey() {
            return "GEOLOCATION";
        }

        @Override
        public String getModuleName() {
            return "Geolocation Shield";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            String country = geolocationService.resolveCountry(ip);
            List<String> blockedCountries = ruleService.getListRule(tenantId, "BLOCKED_COUNTRIES");
            if (blockedCountries.contains(country)) {
                return 100; // Block score weight for forbidden countries
            }
            return 0;
        }
    }

    @Service
    public static class TorExitNodeModule implements SecurityDetectionModule {
        private final TorExitNodeRepository torExitNodeRepository;

        public TorExitNodeModule(TorExitNodeRepository torExitNodeRepository) {
            this.torExitNodeRepository = torExitNodeRepository;
        }

        @Override
        public String getModuleKey() {
            return "TOR_EXIT_NODE";
        }

        @Override
        public String getModuleName() {
            return "Tor Exit Node Protection";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            if (torExitNodeRepository.existsByIpAddress(ip)) {
                return 100; // Tor exit nodes are highly suspicious
            }
            return 0;
        }
    }

    @Service
    public static class VpnDetectionModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "VPN_DETECTION";
        }

        @Override
        public String getModuleName() {
            return "VPN & Datacenter Detector";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            // Mock hosting datacenters & proxy networks
            if (ip.startsWith("185.190.") || ip.startsWith("82.102.") || ip.startsWith("45.132.")) {
                return 80; // High probability VPN/proxy range
            }
            return 0;
        }
    }

    @Service
    public static class ThreatIntelligenceModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "THREAT_INTELLIGENCE";
        }

        @Override
        public String getModuleName() {
            return "Threat Intelligence Feed Checker";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            // Mock known botnets and attacker feeds (AbuseIPDB, Cisco Talos)
            if (ip.startsWith("203.0.113.") || ip.startsWith("198.51.100.")) {
                return 100; // Active blacklist indicators
            }
            return 0;
        }
    }

    @Service
    public static class AsnReputationModule implements SecurityDetectionModule {
        @Override
        public String getModuleKey() {
            return "ASN_REPUTATION";
        }

        @Override
        public String getModuleName() {
            return "ASN Reputation Monitor";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            // Simulates bad ASN reputation network checks
            if (ip.startsWith("195.") || ip.startsWith("95.")) {
                return 30; // Block ASN hosting nodes
            }
            return 0;
        }
    }

    @Service
    public static class IpReputationModule implements SecurityDetectionModule {
        private final AttackLogRepository attackLogRepository;

        public IpReputationModule(AttackLogRepository attackLogRepository) {
            this.attackLogRepository = attackLogRepository;
        }

        @Override
        public String getModuleKey() {
            return "IP_REPUTATION";
        }

        @Override
        public String getModuleName() {
            return "IP Attack History Reputation";
        }

        @Override
        public int evaluateRisk(String tenantId, String ip, VisitorMetadata metadata) {
            long attackCount = attackLogRepository.countByTenantIdAndIp(tenantId, ip);
            if (attackCount > 20) {
                return 100;
            } else if (attackCount > 5) {
                return 50;
            }
            return 0;
        }
    }
}
