package com.example.demo.model;

import com.example.demo.constant.BookStatus;
import com.example.demo.model.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty(message = "Name cannot be empty")
    @Size(max = 255, message = "Name can be at most 255 characters")
    private String name;
    @NotEmpty(message = "Author cannot be empty")
    @Size(max = 255, message = "Author can be at most 255 characters")
    private String author;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;


    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 9999, message = "Publication year must be a 4-digit number")
    private Integer publicationYear;
    @Size(max = 2000, message = "Description can be at most 2000 characters")
    private String description;
    @NotNull(message = "Status is required")
    private BookStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(name = "book_cover", length = 255)
    private String bookCover;
    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;
    @OneToMany(mappedBy = "book")
    private List<OrderItem> orderItems;
    @ManyToMany
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

}
