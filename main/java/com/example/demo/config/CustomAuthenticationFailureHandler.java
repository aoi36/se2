package com.example.demo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        System.err.println("Authentication Failed: " + exception.getMessage());
        System.err.println("Exception type: " + exception.getClass().getName());
        if (exception.getCause() != null) {
            System.err.println("Cause: " + exception.getCause().getClass().getName());
        }

        String errorMessage = "Invalid credentials!";

        if (exception instanceof DisabledException) {
            errorMessage = "Your account is inactive. Please contact support.";
        } else if (exception instanceof LockedException) {
            errorMessage = "Your account is locked. Contact the administrator.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid username or password!";
        } else if (exception instanceof InternalAuthenticationServiceException && exception.getCause() instanceof DisabledException) {
            errorMessage = "Your account is inactive. Please contact support.";
        }


        String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());

        redirectStrategy.sendRedirect(request, response, "/sign-in?error=" + encodedErrorMessage);
    }
}