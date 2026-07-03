package com.securegate.ai;

import com.securegate.ai.dto.AccessDecision;
import com.securegate.ai.service.DecisionEngine;
import com.securegate.ai.service.GeolocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private GeolocationService geolocationService;

	@Autowired
	private DecisionEngine decisionEngine;

	@Test
	void contextLoads() {
		assertNotNull(geolocationService);
		assertNotNull(decisionEngine);
	}

	@Test
	void testCidrMatching() {
		// Valid matches
		assertTrue(geolocationService.ipMatchesCidr("192.168.1.5", "192.168.1.0/24"));
		assertTrue(geolocationService.ipMatchesCidr("10.50.32.1", "10.0.0.0/8"));
		assertTrue(geolocationService.ipMatchesCidr("172.16.50.10", "172.16.0.0/16"));
		assertTrue(geolocationService.ipMatchesCidr("192.168.1.5", "192.168.1.5")); // Direct IP match

		// Invalid matches
		assertFalse(geolocationService.ipMatchesCidr("192.168.2.5", "192.168.1.0/24"));
		assertFalse(geolocationService.ipMatchesCidr("11.50.32.1", "10.0.0.0/8"));
		assertFalse(geolocationService.ipMatchesCidr("172.17.50.10", "172.16.0.0/16"));
	}

	@Test
	void testWhitelistWinsOverBlacklist() {
		String testIp = "192.168.1.222";
		
		// Whitelist the IP under the default system tenant
		decisionEngine.whitelistIP(com.securegate.ai.service.RuleService.DEFAULT_TENANT_ID, testIp, "Admin Safe Laptop");
		
		// Blacklist the same IP under the default system tenant
		decisionEngine.blockIP(com.securegate.ai.service.RuleService.DEFAULT_TENANT_ID, testIp, "Simulated Malicious Activity", "PERMANENT", null);
		
		// Check access - Whitelist must win!
		AccessDecision decision = decisionEngine.checkAccess(com.securegate.ai.service.RuleService.DEFAULT_TENANT_ID, testIp);
		assertTrue(decision.allowed(), "Whitelisted IP must be allowed even if in blacklist.");
		assertEquals("IP is whitelisted", decision.reason());
		
		// Clean up
		decisionEngine.removeFromWhitelist(com.securegate.ai.service.RuleService.DEFAULT_TENANT_ID, testIp);
		decisionEngine.unblockIP(com.securegate.ai.service.RuleService.DEFAULT_TENANT_ID, testIp);
	}
}

