package com.example.iocommunication;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatName;

    @ManyToOne
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @ManyToMany
    private List<User> usersInChat = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Message> messages = new ArrayList<>();

    public Chat() {}

    public Chat(String chatName, User createdBy, Date dateCreated, List<User> usersInChat, List<Message> messages) {
        this.chatName = chatName;
        this.createdBy = createdBy;
        this.dateCreated = dateCreated;
        this.usersInChat = usersInChat != null ? usersInChat : new ArrayList<>();
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public String getChatName() {
        return chatName;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public List<User> getAllUsers() {
        return usersInChat != null ? usersInChat : new ArrayList<>();
    }

    public List<Message> getAllMessages() {
        return messages != null ? messages : new ArrayList<>();
    }

    public List<File> getAllFiles() {
        if (messages == null) return new ArrayList<>();
        return messages.stream()
                .flatMap(msg -> msg.getAttachments().stream())
                .toList();
    }

    public void addUser(User user) {
        if (!usersInChat.contains(user)) usersInChat.add(user);
    }

    public void removeUser(User user) {
        usersInChat.remove(user);
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public void removeMessage(Message msg) {
        messages.remove(msg);
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setUsersInChat(List<User> usersInChat) {
        this.usersInChat = usersInChat != null ? usersInChat : new ArrayList<>();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }
}
