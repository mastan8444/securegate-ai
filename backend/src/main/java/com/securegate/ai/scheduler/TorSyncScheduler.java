package com.securegate.ai.scheduler;

import com.securegate.ai.entity.TorExitNode;
import com.securegate.ai.repository.TorExitNodeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TorSyncScheduler {

    private final TorExitNodeRepository torExitNodeRepository;

    public TorSyncScheduler(TorExitNodeRepository torExitNodeRepository) {
        this.torExitNodeRepository = torExitNodeRepository;
    }

    @PostConstruct
    public void init() {
        // Pre-seed mock Tor IP for local simulation tests
        seedMockTorNode("100.100.100.100");
        // Start async fetch
        new Thread(this::fetchTorExitNodes).start();
    }

    private void seedMockTorNode(String ip) {
        if (!torExitNodeRepository.existsByIpAddress(ip)) {
            torExitNodeRepository.save(new TorExitNode(ip, LocalDateTime.now()));
            System.out.println("[Tor Sync] Pre-seeded mock Tor Exit Node: " + ip);
        }
    }

    /**
     * Downloads and parses Tor Exit Node IPs every 24 hours at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchTorExitNodes() {
        System.out.println("[Tor Sync] Starting daily Tor exit node list download...");
        List<String> ips = new ArrayList<>();
        try {
            URL url = new URL("https://check.torproject.org/exit-addresses");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("ExitAddress")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            ips.add(parts[1]);
                        }
                    }
                }
            }

            if (!ips.isEmpty()) {
                System.out.println("[Tor Sync] Successfully parsed " + ips.size() + " Tor Exit IPs. Saving to database.");
                // Batch insert/update
                for (String ip : ips) {
                    if (!torExitNodeRepository.existsByIpAddress(ip)) {
                        torExitNodeRepository.save(new TorExitNode(ip, LocalDateTime.now()));
                    }
                }
                System.out.println("[Tor Sync] Database updated successfully.");
            }
        } catch (Exception e) {
            System.err.println("[Tor Sync] Failed to download Tor exit list: " + e.getMessage() + ". Continuing with existing cache.");
        }
    }
}
