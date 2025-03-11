package com.example.demo.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String administratorName;
    private String contactNumber;
    @OneToMany(mappedBy = "provider")
    List<Book> books;
}
