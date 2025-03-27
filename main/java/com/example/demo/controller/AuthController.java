package com.example.demo.controller;

import com.example.demo.Service.UserService;
import com.example.demo.model.User.Role;
import com.example.demo.model.User.User;
import com.example.demo.constant.RoleName;
import com.example.demo.exception.UserNotFoundException;

import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.User.UserTemplate;
import com.example.demo.util.Utility;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import net.bytebuddy.utility.RandomString;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

@Controller
public class AuthController {

    @Autowired
    PasswordEncoder encoder;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JavaMailSender mailSender;


    public void createSampleUser() {
        // Create the ADMIN role if it does not exist
        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
        if (userRepository.findAll().isEmpty()) {

            userService.createUser("Admin1", "Admin1", RoleName.ROLE_ADMIN);
        }
    }

    @GetMapping("/sign-in")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        createSampleUser();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/book/list";
        }
        return "Auth/sign-in";
    }

    @GetMapping("/member")
    @PreAuthorize("hasRole('ADMIN')")
    public String loginSuccess(Model model, Authentication authentication, Principal principal) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            authentication.getAuthorities().forEach(authority ->
                    System.out.println("User Role: " + authority.getAuthority())
            );
        }
        return "member";
    }

    @GetMapping("/register")
    public String register(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/book/list";
        }
        model.addAttribute("user", new UserTemplate());
        return "Auth/register";
    }

    @PostMapping("/register")
    public String registerHandle(@Valid @ModelAttribute("user") UserTemplate ut,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("User", ut);
            return "Auth/register";
        }

        if (userRepository.findByUsername(ut.getUsername()).isPresent()) {
            result.rejectValue("username", "error.username", "Username already exists");
            // Optionally print this error immediately
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("User", ut);
            return "Auth/register";
        } else {
            String name = ut.getUsername();
            String password = ut.getPassword();

            userRepository.save(new User(name, encoder.encode(password)));
            model.addAttribute("User", new UserTemplate());
            model.addAttribute("success", true);
            return "Auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "Auth/forgotPasswordForm";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String token = RandomString.make(30);

        try {
            userService.updateResetPasswordToken(token, email);
            String resetPasswordLink = Utility.getSiteURL(request) + "/reset-password?token=" + token;
            sendEmail(email, resetPasswordLink);
            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");

        } catch (UserNotFoundException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (UnsupportedEncodingException | MessagingException e) {
            model.addAttribute("error", "Error while sending email");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "Auth/forgotPasswordForm";
    }

    public void sendEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("contact@gmail.com", "CHANGE PASSWORD");
        helper.setTo(recipientEmail);

        String subject = "Here's the link to reset your password";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + link + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "Auth/resetPasswordForm";
        }

        return "Auth/resetPasswordForm";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        model.addAttribute("title", "Reset your password");

        if (!password.equals(confirmPassword)) {
            model.addAttribute("message", "Passwords do not match");
            return "Auth/resetPasswordForm";
        }
        if (!password.matches("^(?=.*\\d)(?=.*[A-Z]).{6,60}$")) {
            model.addAttribute("message", "Password must be 6-60 characters and contain at least 1 digit and 1 uppercase letter");
            return "Auth/resetPasswordForm";
        }
        User user = userService.getByResetPasswordToken(token);
        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "Auth/forgotPasswordForm";
        }
        userService.updatePassword(user, password);
        model.addAttribute("message", "You have successfully changed your password.");
        return "message";
    }
}
