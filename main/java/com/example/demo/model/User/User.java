package com.example.demo.model.User;

import com.example.demo.model.Book;
import com.example.demo.model.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 6, max = 60, message = "Username must be between 6 and 60 characters")
    private String username;
    @Column(length = 255)
    @NotEmpty(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).{6,60}$",
            message = "Password must be 6-60 characters and contain at least 1 digit and 1 uppercase letter")
    private String password;
    private String name;
    private String email;
    @Column(length = 255)
    private String avatar;
    @Column(length = 255)
    private String resetPasswordToken;
    @Column(length = 15)
    private String tel;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
    @OneToMany(mappedBy = "user")
    private List<Book> books;

    public User() {
    }
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
