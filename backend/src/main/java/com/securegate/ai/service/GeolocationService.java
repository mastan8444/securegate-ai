package com.securegate.ai.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class GeolocationService {

    @Value("${securegate.geoip.database-path}")
    private String databasePath;

    private DatabaseReader databaseReader = null;

    @PostConstruct
    public void initMaxMind() {
        try {
            File database = new File(databasePath);
            if (database.exists()) {
                databaseReader = new DatabaseReader.Builder(database).build();
                System.out.println("MaxMind GeoIP Database successfully loaded from " + databasePath);
            } else {
                System.out.println("MaxMind GeoIP database file not found at: " + databasePath + ". Using local mock country resolver.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load MaxMind GeoIP Database: " + e.getMessage() + ". Defaulting to local mock country resolver.");
        }
    }

    /**
     * Resolves an IP to a country code using MaxMind database with a deterministic mock resolver fallback.
     */
    public String resolveCountry(String ip) {
        if (ip == null || ip.isBlank()) {
            return "UNKNOWN";
        }

        // Try MaxMind first
        if (databaseReader != null) {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);
                CountryResponse response = databaseReader.country(ipAddress);
                String countryCode = response.getCountry().getIsoCode();
                if (countryCode != null && !countryCode.isBlank()) {
                    return countryCode;
                }
            } catch (Exception e) {
                // If it fails (private IP, not in database, etc.), fall through to mock
            }
        }
        
        // Match private networks
        if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.")) {
            return "LOCAL";
        }

        // Deterministic mock mapping based on IP segments for simulation
        if (ip.startsWith("195.") || ip.startsWith("95.")) {
            return "RU"; // Russia
        }
        if (ip.startsWith("222.") || ip.startsWith("119.")) {
            return "CN"; // China
        }
        if (ip.startsWith("175.45.176.")) {
            return "KP"; // North Korea
        }

        // Fallback: Deterministic country selection based on the hash of the IP
        int hash = Math.abs(ip.hashCode());
        String[] countries = {"IN", "US", "GB", "DE", "FR", "CA", "SG", "AU"};
        return countries[hash % countries.length];
    }

    /**
     * Checks if a given IP address belongs to a CIDR range (e.g., 192.168.1.0/24).
     */
    public boolean ipMatchesCidr(String ipAddress, String cidr) {
        try {
            if (!cidr.contains("/")) {
                return ipAddress.equals(cidr);
            }

            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String subnetString = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress subnetAddress = InetAddress.getByName(subnetString);
            InetAddress ip = InetAddress.getByName(ipAddress);

            byte[] subnetBytes = subnetAddress.getAddress();
            byte[] ipBytes = ip.getAddress();

            if (subnetBytes.length != ipBytes.length) {
                return false; // IPv4 vs IPv6 mismatch
            }

            int bytesCount = prefixLength / 8;
            for (int i = 0; i < bytesCount; i++) {
                if (subnetBytes[i] != ipBytes[i]) {
                    return false;
                }
            }

            int remainingBits = prefixLength % 8;
            if (remainingBits > 0) {
                byte mask = (byte) (0xFF00 >> remainingBits);
                if ((subnetBytes[bytesCount] & mask) != (ipBytes[bytesCount] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }
}
