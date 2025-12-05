public class AutomaticReaction {
    private final Integer reactionId;
    private String name;

    public AutomaticReaction(Integer reactionId, String name) {
        this.reactionId = reactionId;
        this.name = name;
    }

    public Integer getReactionId() {
        return reactionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
