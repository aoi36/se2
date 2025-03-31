package com.example.demo.model;

import com.example.demo.model.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Category name cannot be empty")
    @Size(min = 5, max = 100, message = "Category name can be between 5 and 100 characters")
    @Column(unique = true, nullable = false)
    private String name;

    private String description;
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();

    public Category(String name, Boolean status, LocalDateTime createdAt) {
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = LocalDateTime.now();
    }
    public Category() {
    }
}
