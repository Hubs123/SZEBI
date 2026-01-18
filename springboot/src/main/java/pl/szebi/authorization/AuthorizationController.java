package pl.szebi.authorization;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.szebi.communication.controller.ChatRestController;
import pl.szebi.communication.model.User;
import pl.szebi.communication.repository.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/szebi")
public class AuthorizationController {
    private static final String SECRET = "CHANGE_ME_SECRET_1234567890123456";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthorizationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @ResponseBody
    public Map<String, String> login(@RequestBody LoginReq r) {
        User user = userRepository.findByUsername(r.username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(r.password, user.getPassword()))
            throw new RuntimeException("Bad credentials");

        String token = Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        return Map.of("token", token);
    }

    @PostMapping("/register")
    @ResponseBody
    public void register(@RequestBody RegisterReq r) {

        if (userRepository.findByUsername(r.username).isPresent())
            throw new RuntimeException("Username exists");

        User u = new User();
        u.setUsername(r.username);
        u.setPassword(passwordEncoder.encode(r.password));
        u.setFirstName(r.firstName);
        u.setLastName(r.lastName);
        u.setRole("ROLE_USER");

        userRepository.save(u);
    }

    public void requireAdmin() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTH = " + auth);
        System.out.println("AUTHORITIES = " + auth.getAuthorities());
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new RuntimeException("ADMIN ONLY");
        }
    }

    public void requireEngineer() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTH = " + auth);
        System.out.println("AUTHORITIES = " + auth.getAuthorities());
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ENGINEER"))) {
            throw new RuntimeException("ENGINEER ONLY");
        }
    }

    static class LoginReq {
        public String username;
        public String password;
    }

    static class RegisterReq {
        public String username;
        public String password;
        public String firstName;
        public String lastName;
    }
}
