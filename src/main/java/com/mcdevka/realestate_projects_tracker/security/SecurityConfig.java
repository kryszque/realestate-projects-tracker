package com.mcdevka.realestate_projects_tracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Mówi Springowi, że to jest klasa konfiguracyjna
@EnableWebSecurity // Włącza obsługę Spring Security
public class SecurityConfig {

    @Bean // Tworzy "regulamin" dla Ochroniarza
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Na razie zezwól na wszystkie żądania bez logowania
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Wyłączamy ochronę CSRF (na razie nieistotna dla API)

        return http.build();
    }
}