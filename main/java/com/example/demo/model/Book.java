package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String author;
    private Double price;
    private Integer stockQuantity;
    private String genre;
    private Integer publicationYear;
    private String description;
    @Column(name = "book_cover", length = 255)
    private String bookCover;
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
    @OneToMany(mappedBy = "book")
    private List<OrderItem> orderItems;


}
