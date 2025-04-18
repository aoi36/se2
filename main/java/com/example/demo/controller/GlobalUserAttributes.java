package com.example.demo.controller;

import com.example.demo.Service.UserService;


import com.example.demo.constant.RoleName;
import com.example.demo.model.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalUserAttributes {
    @Autowired
    private UserService userService;
    @ModelAttribute
    public void addUserAttributes(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            if (user != null) {
                String name = user.getUsername();
                RoleName roleName = user.getRole().getName();
                boolean isAdmin = roleName == RoleName.ROLE_ADMIN;
                model.addAttribute("userId", user.getId());
                model.addAttribute("avatar", user.getAvatar());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("name", name);
                model.addAttribute("isAdmin", isAdmin);
            }
        }
    }
}