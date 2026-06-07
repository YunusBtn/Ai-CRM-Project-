package com.yunus.config;

import com.yunus.auth.entity.Role;
import com.yunus.auth.entity.User;
import com.yunus.auth.repository.RoleRepository;
import com.yunus.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Rolleri oluştur
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Role {} created", roleName);
            }
        }

        // Admin kullanıcısı yoksa oluştur
        if (userRepository.findByEmail("admin@crm.com").isEmpty()) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                    .orElseThrow();

            User admin = User.builder()
                    .email("admin@crm.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .isActive(true)
                    .isDeleted(false)
                    .role(adminRole)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created");
        }
    }
}
