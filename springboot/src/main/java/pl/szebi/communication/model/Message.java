package pl.szebi.communication.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User sender;

    @OneToMany(cascade = CascadeType.ALL)
    private List<File> attachments;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Message() {}

    public Message(User sender, List<File> attachments, Date dateCreated, String content) {
        this.sender = sender;
        this.attachments = attachments;
        this.dateCreated = dateCreated;
        this.content = content;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public String getContent() {
        return content;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
