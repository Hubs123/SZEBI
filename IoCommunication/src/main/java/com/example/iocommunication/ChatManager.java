package com.example.iocommunication;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ChatManager {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FileRepository fileRepository;

    public ChatManager(ChatRepository chatRepository,
                       UserRepository userRepository,
                       MessageRepository messageRepository,
                       FileRepository fileRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.fileRepository = fileRepository;
    }

    public Chat dbCreateChat(String chatName, User creator) {
        if (chatRepository.findByChatName(chatName).isPresent()) {
            throw new IllegalArgumentException("Chat with this name already exists");
        }
        Chat chat = new Chat();
        chat.setChatName(chatName);
        chat.setCreatedBy(creator);
        chat.setDateCreated(new Date());
        chat.setUsersInChat(List.of(creator));
        return chatRepository.save(chat);
    }

    public void dbDeleteChat(Chat chat) {
        chatRepository.delete(chat);
    }

    public List<Chat> dbGetUserChats(User user) {
        return chatRepository.findByUsersInChatContains(user);
    }

    public Optional<Chat> getChat(Long id) {
        return chatRepository.findById(id);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void dbAddUserToChat(Chat chat, User user) {
        if (!chat.getAllUsers().contains(user)) {
            chat.getAllUsers().add(user);
            chatRepository.save(chat);
        }
    }

    public void dbRemoveUserFromChat(Chat chat, User user) {
        chat.getAllUsers().remove(user);
        chatRepository.save(chat);
    }

    public void dbAddMessageToChat(Chat chat, Message msg) {
        msg.setDateCreated(new Date());
        messageRepository.save(msg);
        chat.getAllMessages().add(msg);
        chatRepository.save(chat);
    }

    public void dbRemoveMessageFromChat(Chat chat, Message msg) {
        chat.getAllMessages().remove(msg);
        chatRepository.save(chat);
        messageRepository.delete(msg);
    }

    public List<Message> dbGetChatHistory(Chat chat) {
        return chat.getAllMessages();
    }

    public List<User> searchUsersByPrefix(String prefix) {
        return userRepository.findByFirstNameStartingWith(prefix);
    }

    @Transactional
    public void dbAddFileToMessage(File file, Message message) {
        fileRepository.save(file);
        message.getAttachments().add(file);
    }

    public void dbRemoveFileFromMessage(Message msg, File file) {
        msg.getAttachments().remove(file);
        messageRepository.save(msg);
        fileRepository.delete(file);
    }

    public Optional<Chat> findByChatName(String name) {
        return chatRepository.findByChatName(name);
    }

    public File getFile(Long fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }
}
