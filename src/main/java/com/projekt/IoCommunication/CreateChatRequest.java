package com.projekt.IoCommunication;

import java.util.List;

public class CreateChatRequest {
    private String chatName;
    private Long creatorId;
    private List<Long> participants;
    public String getChatName() {
        return chatName;
    }
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    public List<Long> getParticipants() {
        return participants;
    }
    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }
}
