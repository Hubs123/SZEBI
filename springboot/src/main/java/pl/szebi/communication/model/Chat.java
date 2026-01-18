package pl.szebi.communication.model;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "chat_users",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> usersInChat = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Message> messages = new ArrayList<>();


    public Long getId() { return id; }
    public String getChatName() { return chatName; }
    public User getCreatedBy() { return createdBy; }
    public Date getDateCreated() { return dateCreated; }
    public List<User> getUsersInChat() { return usersInChat; }
    public List<Message> getMessages() { return messages; }


    public void setId(Long id) { this.id = id; }
    public void setChatName(String chatName) { this.chatName = chatName; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }
    public void setUsersInChat(List<User> users) {
        usersInChat.clear();
        if (users != null) usersInChat.addAll(users);
    }
    public void setMessages(List<Message> messages) {
        this.messages.clear();
        if (messages != null) this.messages.addAll(messages);
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
}
