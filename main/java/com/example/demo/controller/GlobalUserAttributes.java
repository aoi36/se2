package com.example.demo.controller;

import com.example.demo.Service.AdminService;
import com.example.demo.model.Administrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalUserAttributes {

    @Autowired
    private AdminService adminService;

    @ModelAttribute
    public void addUserAttributes(Model model, Principal principal) {
        if (principal != null) {
            Administrator admin = adminService.findByUsername(principal.getName());
            if (admin != null) {
                model.addAttribute("avatar", admin.getAvatar());
                model.addAttribute("email", admin.getEmail());
                model.addAttribute("name", admin.getAdministratorName());

            }
        }
    }
}