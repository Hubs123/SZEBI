package pl.szebi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String SECRET = "CHANGE_ME_SECRET_1234567890123456";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        OncePerRequestFilter jwtFilter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain) throws ServletException, IOException {

                String path = req.getRequestURI();

                if (path.equals("/") ||
                        path.equals("/index.html") ||
                        path.equals("/login.html") ||
                        path.equals("/register.html") ||
                        path.endsWith(".js") ||
                        path.endsWith(".css") ||
                        path.startsWith("/api/szebi/login") ||
                        path.startsWith("/api/szebi/register")) {

                    chain.doFilter(req, res);
                    return;
                }

                String header = req.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    try {
                        String token = header.substring(7);
                        Claims claims = Jwts.parserBuilder()
                                .setSigningKey(SECRET_KEY)
                                .build()
                                .parseClaimsJws(token)
                                .getBody();

                        Long userId = Long.parseLong(claims.getSubject());
                        String role = claims.get("role", String.class);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userId.toString(),
                                        null,
                                        List.of(new SimpleGrantedAuthority(role))
                                );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } catch (Exception e) {
                        SecurityContextHolder.clearContext();
                    }
                }

                chain.doFilter(req, res);
            }
        };

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index.html", "/login.html", "/register.html",
                                "/script.js", "/auth.js", "/styles.css",
                                "/js/**", "/css/**", "/images/**",
                                "/api/chat/files/**"
                        ).permitAll()
                        .requestMatchers("/api/szebi/login", "/api/szebi/register").permitAll()
                        .requestMatchers("/api/chat/addUser", "/api/chat/create").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/chat/**", "/api/chat/**").authenticated()
                        .requestMatchers("/api/chat/files/**").permitAll()
                        .requestMatchers("/api/data/**").authenticated()
                        .requestMatchers("/api/control/**").permitAll()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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

