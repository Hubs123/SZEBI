package com.example.iocommunication.service;

import com.example.iocommunication.model.File;
import com.example.iocommunication.model.Message;
import com.example.iocommunication.model.User;
import com.example.iocommunication.model.Chat;
import com.example.iocommunication.repository.ChatRepository;
import com.example.iocommunication.repository.FileRepository;
import com.example.iocommunication.repository.MessageRepository;
import com.example.iocommunication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
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
        chat.addUser(creator);
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

    public Optional<Chat> findByChatName(String name) {
        return chatRepository.findByChatName(name);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void dbAddUserToChat(Chat chat, User user) {
        chat.addUser(user);
        chatRepository.save(chat);
    }

    public void dbRemoveUserFromChat(Chat chat, User user) {
        chat.removeUser(user);
        chatRepository.save(chat);
    }

    public List<User> getUsersInChat(Chat chat) {
        return new ArrayList<>(chat.getUsersInChat());
    }

    public List<User> searchUsersByPrefix(String prefix) {
        return userRepository.findByFirstNameStartingWith(prefix);
    }

    public void dbAddMessageToChat(Chat chat, Message msg) {
        msg.setDateCreated(new Date());
        messageRepository.save(msg);
        chat.addMessage(msg);
        chatRepository.save(chat);
    }

    public void dbRemoveMessageFromChat(Chat chat, Message msg) {
        chat.removeMessage(msg);
        chatRepository.save(chat);
        messageRepository.delete(msg);
    }

    public List<Message> dbGetChatHistory(Chat chat) {
        return new ArrayList<>(chat.getMessages());
    }

    @Transactional
    public void dbAddFileToMessage(File file, Message message) {
        fileRepository.save(file);
        message.getAttachments().add(file);
    }

    public ChatRepository getChatRepository() {
        return chatRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public void dbRemoveFileFromMessage(Message msg, File file) {
        msg.getAttachments().remove(file);
        messageRepository.save(msg);
        fileRepository.delete(file);
    }

    public File getFile(Long fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }
}
