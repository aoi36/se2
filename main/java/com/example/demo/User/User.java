package com.example.demo.User;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    private String roles;
    private String address;
    private String email;
    private String resetPasswordToken;

    public User() {
    }

    public User(String username, String password, String address) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.roles = "CUSTOMER";
    }

}
