package com.example.demo.controller;

import com.example.demo.Service.UserService;
import com.example.demo.User.MyUserDetail;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.User.User;
import com.example.demo.User.UserTemplate;
import com.example.demo.util.Utility;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import net.bytebuddy.utility.RandomString;
import java.io.UnsupportedEncodingException;

@Controller
public class AuthController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    UserService userService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/sign-in")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/member"; // Redirect to home if already logged in
        }

        return "sign-in"; // Show login page only if not authenticated
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
        model.addAttribute("user", new UserTemplate());
        return "register";
    }
    @PostMapping("/register")
    public String registerHandle(@Valid UserTemplate ut,
                                 BindingResult result,
                                 Model model) {

        // Debug print
        System.out.println("Has errors: " + result.hasErrors());
        if (userRepository.findByUsername(ut.getUsername()).isPresent()) {
            result.rejectValue("username", "error.username", "Username already exists");
            model.addAttribute("user", ut);
            return "register";
        }
        if (result.hasErrors()) {
            // Print each error
            result.getAllErrors().forEach(error -> {
                System.out.println("Field: " + ((FieldError) error).getField());
                System.out.println("Message: " + error.getDefaultMessage());
            });

            // Add both the user object and the binding result to the model
            model.addAttribute("user", ut);
            model.addAttribute("org.springframework.validation.BindingResult.user", result);
            return "register";
        } else {
            String name = ut.getUsername();
            String password = ut.getPassword();
            String address = ut.getAddress();
            userRepository.save(new User(name, encoder.encode(password), address));
            model.addAttribute("user", new UserTemplate());
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
        User user= userService.getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "resetPasswordForm";
        }

        return "resetPasswordForm";
    }
    @PostMapping("/reset-password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");

        User user = userService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "forgotPasswordForm";
        } else {
            userService.updatePassword(user, password);

            model.addAttribute("message", "You have successfully changed your password.");
        }

        return "message";
    }
}
