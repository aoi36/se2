package com.example.demo.controller;

import com.example.demo.Service.AdminService;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Administrator;
import com.example.demo.repository.AdminRepository;
import com.example.demo.User.UserTemplate;
import com.example.demo.util.Utility;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import net.bytebuddy.utility.RandomString;
import java.io.UnsupportedEncodingException;

@Controller
public class AuthController {

    @Autowired
    PasswordEncoder encoder;
    @Autowired
    AdminService adminService;
    @Autowired
    AdminRepository adminRepo;
    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/sign-in")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/member";
        }

        return "sign-in";
    }


    @GetMapping("/member")
    public String loginSuccess(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);
        }
        return "member";
    }

    @GetMapping("/register")
    public String register(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/member";
        }
        model.addAttribute("administrator", new UserTemplate());
        return "register";
    }
    @PostMapping("/register")
    public String registerHandle(@Valid @ModelAttribute("administrator") UserTemplate ut,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("administrator", ut);
            return "register";
        }

        // Then check for business logic errors like duplicate username
        if (adminRepo.findByUsername(ut.getUsername()).isPresent()) {
            result.rejectValue("username", "error.username", "Username already exists");
            // Optionally print this error immediately
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });
            model.addAttribute("administrator", ut);
            return "register";
        }
         else {
            String name = ut.getUsername();
            String password = ut.getPassword();

            adminRepo.save(new Administrator(name, encoder.encode(password)));
            model.addAttribute("administrator", new UserTemplate());
            model.addAttribute("success", true);
            return "register";
        }
    }
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgotPasswordForm";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String token = RandomString.make(30);

        try {
            adminService.updateResetPasswordToken(token, email);
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

        return "forgotPasswordForm";
    }
    public void sendEmail(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("contact@gmail.com", "XEM HOLOLIVE IT THOI");
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
        Administrator admin= adminService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (admin == null) {
            model.addAttribute("message", "Invalid Token");
            return "resetPasswordForm";
        }

        return "resetPasswordForm";
    }
    @PostMapping("/reset-password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");

        Administrator administrator = adminService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");

        if (administrator == null) {
            model.addAttribute("message", "Invalid Token");
            return "forgotPasswordForm";
        } else {
            adminService.updatePassword(administrator, password);

            model.addAttribute("message", "You have successfully changed your password.");
        }

        return "message";
    }
}
