package com.example.demo.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String administratorName;
    private String contactNumber;

    @OneToMany(mappedBy = "administrator")
    List<Book> books;
    @OneToMany(mappedBy = "administrator")
    List<Order> orders;
}
