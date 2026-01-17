package pl.szebi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Wyłączamy CSRF dla API
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Konfiguracja CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless dla API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**", "/login", "/logout", "/error").permitAll() // Pozwól na dostęp do wszystkich endpointów API
                .anyRequest().permitAll() // Tymczasowo pozwól na wszystkie żądania
            );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Zezwól na wszystkie źródła
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*")); // Zezwól na wszystkie nagłówki
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type")); // Udostępnij nagłówki
        configuration.setAllowCredentials(false); // Nie używamy credentials (cookies) przy "*" origins
        configuration.setMaxAge(3600L); // Cache preflight requests przez 1 godzinę

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Zastosuj do wszystkich ścieżek
        return source;
    }
}

