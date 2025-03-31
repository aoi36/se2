package com.example.demo.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdminOrProvider = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_PROVIDER"));
            if (isAdminOrProvider) {
                return "redirect:/book/list";
            }
        }
        return "Error/loginAccessDenied";
    }

    @GetMapping("/error")
    public String handleError() {
        return "Error/error";
    }
}