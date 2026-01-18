package pl.szebi.communication.controller;
import pl.szebi.communication.model.Message;
import pl.szebi.communication.model.User;
import pl.szebi.communication.model.Chat;
import pl.szebi.communication.model.File;
import pl.szebi.communication.repository.UserRepository;
import pl.szebi.communication.service.ChatManager;
import pl.szebi.authorization.AuthorizationController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/api")
public class ChatRestController {
    private final UserRepository userRepository;
    private final ChatManager chatManager;
    private final AuthorizationController authorizationController;

    public ChatRestController(UserRepository userRepository, ChatManager chatManager, AuthorizationController authorizationController) {
        this.userRepository = userRepository;
        this.chatManager = chatManager;
        this.authorizationController = authorizationController;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register.html";
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat.html";
    }

    @GetMapping("/chat/searchUsers")
    @ResponseBody
    public List<User> searchUsers(@RequestParam String prefix) {
        return chatManager.searchUsersByPrefix(prefix);
    }

    @PostMapping("/chat/create")
    @ResponseBody
    public Chat createChat(@RequestBody Map<String, Object> body) {
        authorizationController.requireAdmin();

        String chatName = (String) body.get("chatName");
        List<String> participants = (List<String>) body.get("participants");

        User creator = getCurrentUser();
        Chat chat = chatManager.dbCreateChat(chatName, creator);

        chatManager.dbAddUserToChat(chat, creator);

        if (participants != null) {
            for (String username : participants) {
                userRepository.findByUsername(username)
                        .ifPresent(u -> chatManager.dbAddUserToChat(chat, u));
            }
        }

        return chat;
    }

    @DeleteMapping("/chat/{chatId}")
    public void deleteChat(@PathVariable Long chatId) {
        authorizationController.requireAdmin();
        chatManager.dbDeleteChat(
                chatManager.getChat(chatId).orElseThrow()
        );
    }

    @PostMapping("/chat/{chatId}/addUser")
    public void addUserToChat(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body
    ) {
        authorizationController.requireAdmin();
        String username = body.get("username");
        User user = userRepository.findByUsername(username).orElseThrow();
        chatManager.dbAddUserToChat(chatManager.getChat(chatId).orElseThrow(), user);
    }

    @DeleteMapping("/chat/{chatId}/users/{userId}")
    public void removeUserFromChat(
            @PathVariable Long chatId,
            @PathVariable Long userId
    ) {
        authorizationController.requireAdmin();
        chatManager.dbRemoveUserFromChat(
                chatManager.getChat(chatId).orElseThrow(),
                chatManager.getUser(userId)
        );

    }
    @GetMapping("/chat/{chatId}/availableUsers")
    @ResponseBody
    public List<User> getAvailableUsers(@PathVariable Long chatId) {
        Chat chat = chatManager.getChat(chatId).orElseThrow();
        return chatManager.getUsersInChat(chat);
    }

    @GetMapping("/chat/all")
    @ResponseBody
    public List<Chat> getChats() {
        return chatManager.dbGetUserChats(getCurrentUser());
    }

    @GetMapping("/chat/{chatId}/messages")
    @ResponseBody
    public List<Message> getMessages(@PathVariable Long chatId) {
        return chatManager.dbGetChatHistory(
                chatManager.getChat(chatId).orElseThrow()
        );
    }
    @PostMapping("/chat/addUser")
    @ResponseBody
    public void addUser(@RequestBody Map<String, String> body) {
        authorizationController.requireAdmin();
        String username = body.get("username");
        String chatName = body.getOrDefault("chatName", "Testowy czat");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chat chat = chatManager.dbGetUserChats(getCurrentUser()).get(0);
        chatManager.dbAddUserToChat(chat, user);
    }
    @PostMapping("/chat/{chatId}/send")
    @ResponseBody
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
            msg.getAttachments().add(f);
        }

        chatManager.dbAddMessageToChat(chat, msg);
        return msg;
    }

    @GetMapping("/chat/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long fileId) {
        File f = chatManager.getFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(f.getFileType()))
                .body(f.getContent());
    }


    @GetMapping("/chat/{userId}/role")
    @ResponseBody
    public Map<String, String> getUserRole(@PathVariable Long userId) {
        User u = chatManager.getUser(userId);
        if (u == null) throw new RuntimeException("User not found");
        return Map.of("role", u.getRole());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        return chatManager.getUser(userId);
    }
}
