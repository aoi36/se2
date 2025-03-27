package com.example.demo.model.User;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserTemplate {
    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 6, max = 60, message = "Username must be between 6 and 60 characters")
    private String username;

    @NotEmpty(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).{6,60}$",
            message = "Password must be 6-60 characters and contain at least 1 digit and 1 uppercase letter")
    private String password;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Email format is invalid"
    )
    @Column(unique = true)
    private String email;
}
