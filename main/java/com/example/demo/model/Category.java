package com.example.demo.model;

import com.example.demo.model.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
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
    @Size(max = 100, message = "Category name can be at most 100 characters")
    @Column(unique = true, nullable = false)
    private String name;
    private String description;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();
}
