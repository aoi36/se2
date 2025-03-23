package com.example.demo.Service;


import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Administrator;
import com.example.demo.repository.AdminRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AdminService {
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public void updateResetPasswordToken(String token, String email) throws UserNotFoundException {
        Administrator admin = adminRepository.findByEmail(email);
        if (admin != null) {
            admin.setResetPasswordToken(token);
            adminRepository.save(admin);
        } else {
            throw new UserNotFoundException("Could not find any user with the email " + email);
        }
        scheduler.schedule(() -> {
            Administrator adminToUpdate = adminRepository.findByEmail(email);
            if (adminToUpdate != null && token.equals(adminToUpdate.getResetPasswordToken())) {
                adminToUpdate.setResetPasswordToken(null);
                adminRepository.save(adminToUpdate);
            }
        }, 5, TimeUnit.MINUTES);
    }

    public Administrator getByResetPasswordToken(String token) {
        return adminRepository.findByResetPasswordToken(token);
    }

    public void updatePassword(Administrator administrator, String newPassword) {

        String encodedPassword = passwordEncoder.encode(newPassword);
        administrator.setPassword(encodedPassword);
        administrator.setResetPasswordToken(null);
        adminRepository.save(administrator);
    }
    public Administrator findByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }

}
