package com.example.iocommunication;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/api")
public class ChatRestController {

    private static final String SECRET = "CHANGE_ME_SECRET";

    private final ChatManager chatManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ChatRestController(
            ChatManager chatManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.chatManager = chatManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @GetMapping("/")
    public String root() {
        return "redirect:/login.html";
    }

    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "register.html";
    }

    @GetMapping("/chat")
    public String chat() {
        return "index.html";
    }
    @PostMapping("/szebi/login")
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
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        return Map.of("token", token);
    }

    @PostMapping("/szebi/register")
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

    /* ===================== CHAT ===================== */

    @GetMapping("/chat/searchUsers")
    public List<User> searchUsers(@RequestParam String prefix) {
        return chatManager.searchUsersByPrefix(prefix);
    }

    @PostMapping("/chat/create")
    public Chat createChat(@RequestBody CreateChatRequest req) {

        requireAdmin();

        User creator = getCurrentUser();
        Chat chat = chatManager.dbCreateChat(req.getChatName(), creator);

        if (req.getParticipants() != null) {
            for (Long uid : req.getParticipants()) {
                User u = chatManager.getUser(uid);
                if (u != null) chatManager.dbAddUserToChat(chat, u);
            }
        }
        return chat;
    }

    @DeleteMapping("/chat/{chatId}")
    public void deleteChat(@PathVariable Long chatId) {
        requireAdmin();
        chatManager.dbDeleteChat(
                chatManager.getChat(chatId).orElseThrow()
        );
    }

    @PostMapping("/chat/{chatId}/addUser")
    public void addUserToChat(
            @PathVariable Long chatId,
            @RequestBody String username
    ) {
        requireAdmin();
        chatManager.dbAddUserToChat(
                chatManager.getChat(chatId).orElseThrow(),
                userRepository.findByUsername(username).orElseThrow()
        );
    }

    @DeleteMapping("/chat/{chatId}/users/{userId}")
    public void removeUserFromChat(
            @PathVariable Long chatId,
            @PathVariable Long userId
    ) {
        requireAdmin();
        chatManager.dbRemoveUserFromChat(
                chatManager.getChat(chatId).orElseThrow(),
                chatManager.getUser(userId)
        );
    }

    @GetMapping("/chat/all")
    public List<Chat> getChats() {
        return chatManager.dbGetUserChats(getCurrentUser());
    }

    @GetMapping("/chat/{chatId}/messages")
    public List<Message> getMessages(@PathVariable Long chatId) {
        return chatManager.dbGetChatHistory(
                chatManager.getChat(chatId).orElseThrow()
        );
    }

    @PostMapping("/chat/{chatId}/send")
    public Message sendMessage(
            @PathVariable Long chatId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file
    ) throws IOException {

        User sender = getCurrentUser();
        Chat chat = chatManager.getChat(chatId).orElseThrow();

        Message msg = new Message(sender, new ArrayList<>(), new Date(),
                content != null ? content : "");

        if (file != null && !file.isEmpty()) {
            File f = new File();
            f.setFileName(file.getOriginalFilename());
            f.setFileType(file.getContentType());
            f.setContent(file.getBytes());
            f.setUploadedBy(sender);
            chatManager.dbAddFileToMessage(f, msg);
        }

        chatManager.dbAddMessageToChat(chat, msg);
        return msg;
    }

    @GetMapping("/chat/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long fileId) {
        File file = chatManager.getFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .body(file.getContent());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        return chatManager.getUser(userId);
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new RuntimeException("ADMIN ONLY");
        }
    }

    /* ===================== DTO ===================== */

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
