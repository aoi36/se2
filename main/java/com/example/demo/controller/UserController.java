package com.example.demo.controller;

import com.example.demo.Service.FileStorageService;
import com.example.demo.Service.UserService;
import com.example.demo.model.Book;
import com.example.demo.model.User.Role;
import com.example.demo.model.User.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    UserRepository userRepository;
    private final UserService userService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String userPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           Model model) {

        Page<User> userPage = userService.getPaginatedUser(PageRequest.of(page, size));
        model.addAttribute("userPage", userPage);
        return "User/userList";
    }
    @GetMapping(value = "detail/{id}")
    public String userDetail(@PathVariable("id") Long id, Model model) {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        boolean isProvider= auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_PROVIDER"));

        if (isProvider && !auth.getName().equals(userRepository.findById(id).orElse(null).getUsername())) {
            model.addAttribute("errorMessage", "You are not authorized to view this page.");
            return "redirect:/book/list";
        }
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            model.addAttribute("errorMessage", "User not found");
            return "User/userList";
        }
        model.addAttribute("user", user);
        return "User/userDetail";
    }
    @GetMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());

        return "User/userAdd";
    }

    @GetMapping(value = "update/{id}")
    public String userUpdate(@PathVariable("id") Long id, Model model) {
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        boolean isProvider= auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_PROVIDER"));

        if (isProvider && !auth.getName().equals(userRepository.findById(id).orElse(null).getUsername())) {
            model.addAttribute("errorMessage", "You are not authorized to view this page.");
            return "redirect:/book/list";
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            model.addAttribute("errorMessage", "User not found");
            return "User/userList";
        }
        List<Book> books = user.getBooks();
        if (!books.isEmpty()) {
            model.addAttribute("books", books);
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "User/userUpdate";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                           @RequestParam(value = "roleId", required = false) Long roleId,
                           Model model) {

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
            return "User/userAdd";
        }

        if (roleId == null) {
            result.rejectValue("role", "error.role", "Role is required");
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
            return "User/userAdd";
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "error.username", "Username already exists");
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
            return "User/userAdd";
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "error.email", "Email already exists");
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
            return "User/userAdd";
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                Path uploadPath = fileStorageService.getFileStorageLocation();
                String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                try (InputStream inputStream = profileImage.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                user.setAvatar(fileName);
                user.setCreatedAt(LocalDateTime.now());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            result.rejectValue("role", "error.role", "Invalid role selected");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userAdd";
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("success", true);
        return "User/userAdd";
    }
    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                             @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                             @RequestParam(value = "roleId", required = false) Long roleId,
                             Model model) {

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }

        if (user.getId() == null) {
            result.reject("error.user", "User id is missing");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }

        Optional<User> existingUserOpt = userRepository.findById(user.getId());
        if (!existingUserOpt.isPresent()) {
            result.reject("error.user", "User not found");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }
        User existingUser = existingUserOpt.get();

        Optional<User> userByUsername = userRepository.findByUsername(user.getUsername());
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(user.getId())) {
            result.rejectValue("username", "error.username", "Username already exists");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }

        Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
            result.rejectValue("email", "error.email", "Email already exists");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                Path uploadPath = fileStorageService.getFileStorageLocation();
                String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                try (InputStream inputStream = profileImage.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                existingUser.setAvatar(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (roleId == null) {
            result.rejectValue("role", "error.role", "Role is required");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            result.rejectValue("role", "error.role", "Invalid role selected");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("user", user);
            return "User/userUpdate";
        }
        existingUser.setRole(role);

        existingUser.setUsername(user.getUsername());
        existingUser.setName(user.getName());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setEmail(user.getEmail());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(existingUser);

        model.addAttribute("user", existingUser);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("success", true);
        return "User/userUpdate";
    }

}
