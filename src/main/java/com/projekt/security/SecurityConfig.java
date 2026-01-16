package com.projekt.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 1. Dostęp tylko dla ADMINA
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 2. Dostęp dla INŻYNIERA (i admina)
                .requestMatchers("/sterowanie/**", "/optimization/**").hasAnyRole("ENGINEER", "ADMIN")
                
                // 3. Dostęp dla MIESZKAŃCA (i pozostałych)
                .requestMatchers("/moje-dane/**").hasAnyRole("USER", "ENGINEER", "ADMIN")
                
                // 4. Strony publiczne (np. strona główna, logowanie)
                .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll()
                
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login") // Twoja strona logowania
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // To jest bardzo ważne! Szyfruje hasła w bazie danych.
        return new BCryptPasswordEncoder();
    }
}