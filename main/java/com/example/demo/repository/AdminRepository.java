package com.example.demo.repository;



import com.example.demo.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByUsername(String username);
    Administrator findByResetPasswordToken(String token);
    Administrator findByEmail(String email);

}
