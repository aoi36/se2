package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Data
@Entity
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String administratorName;
    private String contactNumber;
    private String username;
    private String password;
    private String email;
    private String resetPasswordToken;
    @OneToMany(mappedBy = "administrator")
    List<OrderInformation> orderInformation;

    public Administrator() {
    }

    public Administrator(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
