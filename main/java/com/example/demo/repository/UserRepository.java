package com.example.demo.repository;

import com.example.demo.model.User.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    User findByResetPasswordToken(String token);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u")
    Page<User> findPageUser(Pageable pageable);;
}
