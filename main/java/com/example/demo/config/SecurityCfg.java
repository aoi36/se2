package com.example.demo.config;

import com.example.demo.Service.JpaUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityCfg {
    @Autowired
    JpaUserDetailsService jpaUserDetailsService;
    @Autowired
    CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .userDetailsService(jpaUserDetailsService)
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/", "/register", "/reset-password", "/forgot-password", "/login", "/access-denied").permitAll()
                        .anyRequest().hasAnyRole("ADMIN", "PROVIDER")
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/sign-in")
                        .loginProcessingUrl("/sign-in")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/sign-in?error=true")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("abcdefgh12345")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(jpaUserDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/sign-in?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/sign-in")
                        )
                        .accessDeniedPage("/access-denied")
                )
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}