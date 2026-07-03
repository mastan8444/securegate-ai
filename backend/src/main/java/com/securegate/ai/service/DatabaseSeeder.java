package com.securegate.ai.service;

import com.securegate.ai.entity.Tenant;
import com.securegate.ai.entity.User;
import com.securegate.ai.repository.TenantRepository;
import com.securegate.ai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          TenantRepository tenantRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Default System Tenant
        if (tenantRepository.findById(RuleService.DEFAULT_TENANT_ID).isEmpty()) {
            System.out.println("Seeding default system tenant...");
            Tenant defaultTenant = new Tenant(
                    RuleService.DEFAULT_TENANT_ID,
                    "System Operations",
                    "sg_live_default_system_secret_key_123456",
                    "ENTERPRISE"
            );
            tenantRepository.save(defaultTenant);
            System.out.println("Default system tenant created successfully!");
        }

        // 2. Seed Default Admin User
        if (userRepository.count() == 0) {
            System.out.println("No users found. Seeding default admin user (admin/admin)...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ROLE_ADMIN");
            admin.setTenantId(RuleService.DEFAULT_TENANT_ID);
            userRepository.save(admin);
            System.out.println("Default admin user created successfully!");
        }
    }
}
