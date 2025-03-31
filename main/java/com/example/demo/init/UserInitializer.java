package com.example.demo.init;

import com.example.demo.constant.RoleName;

import com.example.demo.model.User.Role;
import com.example.demo.model.User.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Order(2)
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Find the ADMIN role
        Optional<Role> adminRoleOpt = roleRepository.findByName(RoleName.ROLE_ADMIN);

        if (adminRoleOpt.isPresent()) {
            Role adminRole = adminRoleOpt.get();

            // Check if any user already has the ADMIN role
            List<User> adminUsers = userRepository.findByRole(adminRole);
            if (adminUsers.isEmpty()) {
                // Create a new admin user if none exist
                User admin = new User();
                admin.setUsername("Admin1");
                admin.setPassword(passwordEncoder.encode("Admin1")); // Encrypt password
                admin.setEmail("admin@example.com");
                admin.setRole(adminRole);
                admin.setStatus(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());

                userRepository.save(admin);
                System.out.println("Admin user created successfully.");
            } else {
                System.out.println("Admin user already exists.");
            }
        } else {
            System.out.println("Admin role not found. Please initialize roles first.");
        }
    }
}
