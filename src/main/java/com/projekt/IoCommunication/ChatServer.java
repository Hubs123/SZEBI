package com.projekt.IoCommunication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServer {

    private final ChatManager manager;

    public ChatServer(ChatManager manager) {
        this.manager = manager;
    }

    public Chat createChat(String chatName, User creator) {
        return manager.dbCreateChat(chatName, creator);
    }

    public void deleteChat(Chat chat) {
        manager.dbDeleteChat(chat);
    }

    public List<Chat> getUserChats(User user) {
        return manager.dbGetUserChats(user);
    }

    public Chat getChat(Long chatId) {
        return manager.getChat(chatId).orElse(null);
    }

    public void addUserToChat(Chat chat, User user) {
        manager.dbAddUserToChat(chat, user);
    }

    public void removeUserFromChat(Chat chat, User user) {
        manager.dbRemoveUserFromChat(chat, user);
    }

    public void addMessageToChat(Chat chat, Message msg) {
        manager.dbAddMessageToChat(chat, msg);
    }

    public void removeMessageFromChat(Chat chat, Message msg) {
        manager.dbRemoveMessageFromChat(chat, msg);
    }

    public List<Message> getChatHistory(Chat chat) {
        return manager.dbGetChatHistory(chat);
    }

    public void addFileToMessage(Message msg, File file) {
        manager.dbAddFileToMessage(file, msg);
    }

    public void removeFileFromMessage(Message msg, File file) {
        manager.dbRemoveFileFromMessage(msg, file);
    }

}
