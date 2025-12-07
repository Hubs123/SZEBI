package com.example.iocommunication;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatManager chatManager;

    public ChatRestController(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @GetMapping("/searchUsers")
    public List<User> searchUsers(@RequestParam String prefix) {
        return chatManager.searchUsersByPrefix(prefix);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long fileId) {
        File file = chatManager.getFile(fileId);
        if (file == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, file.getFileType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getContent());
    }

    @PostMapping("/create")
    public Chat createChat(@RequestBody CreateChatRequest req) {
        if (req.getChatName() == null || req.getChatName().isBlank())
            throw new RuntimeException("Chat name cannot be empty");

        User creator = chatManager.getUser(req.getCreatorId());
        if (creator == null) throw new RuntimeException("Creator not found");

        Chat chat = chatManager.dbCreateChat(req.getChatName().trim(), creator);

        if (req.getParticipants() != null) {
            for (Long uid : req.getParticipants()) {
                User u = chatManager.getUser(uid);
                if (u != null) chatManager.dbAddUserToChat(chat, u);
            }
        }
        return chat;
    }

    @GetMapping("/all")
    public List<Chat> getAllChats() {
        User user = chatManager.getUser(1L);
        if (user == null) return List.of();
        return chatManager.dbGetUserChats(user);
    }

    @GetMapping("/{chatId}/messages")
    public List<Message> getMessages(@PathVariable Long chatId) {
        return chatManager.getChat(chatId)
                .map(chatManager::dbGetChatHistory)
                .orElse(List.of());
    }

    @PostMapping("/{chatId}/send")
    public Message sendMessage(@PathVariable Long chatId,
                               @RequestParam Long userId,
                               @RequestParam(required = false) String content,
                               @RequestParam(required = false) MultipartFile file) throws IOException {

        Chat chat = chatManager.getChat(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        User sender = chatManager.getUser(userId);

        if (sender == null) throw new RuntimeException("User not found");

        Message msg = new Message(sender, new ArrayList<>(), new Date(), content != null ? content : "");

        if (file != null && !file.isEmpty()) {
            File f = new File();
            f.setFileName(file.getOriginalFilename());
            f.setFileType(file.getContentType());
            f.setSize(file.getSize());
            f.setUploadedBy(sender);
            f.setContent(file.getBytes());

            chatManager.dbAddFileToMessage(f, msg);
        }

        chatManager.dbAddMessageToChat(chat, msg);
        return msg;
    }


}
