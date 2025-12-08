package com.projekt.alerts;

public class AutomaticReaction {
    private final Integer reactionId;
    private String reactionName;

    public AutomaticReaction(Integer reactionId, String reactionName) {
        this.reactionId = reactionId;
        this.reactionName = reactionName;
    }

    public Integer getReactionId() {
        return reactionId;
    }

    public String getReactionName() {
        return reactionName;
    }

    public void setReactionName(String reactionName) {
        this.reactionName = reactionName;
    }

//    metoda execute będzie dostosowana do potrzeb przy połączeniu z modułem sterowania
//    public void executeReaction(alerty.Device device, String reactionName) {}
}
