package com.healthinsurance.identity.config;

import com.healthinsurance.identity.entity.Role;
import com.healthinsurance.identity.entity.User;
import com.healthinsurance.identity.repository.RoleRepository;
import com.healthinsurance.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final List<String> SYSTEM_ROLES = Arrays.asList(
            "CUSTOMER",
            "AGENT",
            "UNDERWRITER",
            "CLAIMS_OFFICER",
            "POLICY_ADMINISTRATOR",
            "FINANCE_OFFICER",
            "HEALTHCARE_PROVIDER",
            "SYSTEM_ADMINISTRATOR"
    );

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultAccounts();
    }

    private void seedRoles() {
        for (String roleName : SYSTEM_ROLES) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName.replace("_", " ") + " Role");
                log.info("SEED: Initialized role [{}]", roleName);
                return roleRepository.save(role);
            });
        }
    }

    private void seedDefaultAccounts() {
        seedUser("admin", "Admin@123", "admin@hims.com", "SYSTEM_ADMINISTRATOR");
        seedUser("underwriter1", "Underwriter@123", "underwriter1@hims.com", "UNDERWRITER");
        seedUser("claims_officer1", "Claims@123", "claims1@hims.com", "CLAIMS_OFFICER");
        seedUser("agent1", "Agent@123", "agent1@hims.com", "AGENT");
        seedUser("finance1", "Finance@123", "finance1@hims.com", "FINANCE_OFFICER");
        seedUser("provider1", "Provider@123", "provider1@cityhospital.com", "HEALTHCARE_PROVIDER");
    }

    private void seedUser(String username, String rawPassword, String email, String roleName) {
        if (!userRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName(roleName);
                newRole.setDescription(roleName + " Role");
                return roleRepository.save(newRole);
            });

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setStatus("ACTIVE");

            userRepository.save(user);
            log.info("SEED: Created default active user [{}] with role [{}]", username, roleName);
        }
    }
}
