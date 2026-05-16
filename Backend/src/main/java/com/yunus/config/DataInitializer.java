package com.yunus.config;

import com.yunus.auth.entity.Role;
import com.yunus.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        for (Role.RoleName roleName : Role.RoleName.values()){
            if (roleRepository.findByName(roleName).isEmpty()){
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Role {} created", roleName);
            }
        }

    }
}
