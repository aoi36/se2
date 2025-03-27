package com.example.demo.Service;


import com.example.demo.model.Order;
import com.example.demo.model.User.Role;

import com.example.demo.model.User.User;
import com.example.demo.constant.RoleName;
import com.example.demo.exception.UserNotFoundException;

import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public void updateResetPasswordToken(String token, String email) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setResetPasswordToken(token);
            userRepository.save(user);
        } else {
            throw new UserNotFoundException("Could not find any user with the email " + email);
        }
        scheduler.schedule(() -> {
            Optional<User> userToUpdateOptional = userRepository.findByEmail(email);
            if (userToUpdateOptional.isPresent()) {
                User userToUpdate = userToUpdateOptional.get();
                if (token.equals(userToUpdate.getResetPasswordToken())) {
                    userToUpdate.setResetPasswordToken(null);
                    userRepository.save(userToUpdate);
                }
            }
        }, 5, TimeUnit.MINUTES);
    }

    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    public void updatePassword(User user, String newPassword) {

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    public void createUser(String username, String rawPassword, RoleName roleName) {
        // Check if the role exists
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role " + roleName + " not found.");
        }

        // Create the user object
        User user = new User();
        user.setUsername(username);
        // Encrypt the password
        user.setPassword(passwordEncoder.encode(rawPassword));
        // Set a display name; here we simply use the username but you can adjust as needed
        user.setName(username);
        // Optionally set email, tel, status, etc.
        user.setStatus(true); // assuming true means active
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(roleOpt.get());

        // Save the user
        userRepository.save(user);
    }

    public Page<User> getPaginatedUser(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
