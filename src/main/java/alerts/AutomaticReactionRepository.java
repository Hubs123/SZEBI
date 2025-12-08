package alerts;

import java.util.List;

public class AutomaticReactionRepository {
    private List<AutomaticReaction> reactions;

    public AutomaticReactionRepository() {
    }

    public List<AutomaticReaction> getAllReactions() {
        return reactions;
    }

    public AutomaticReaction getReactionById(int id) {
        for (AutomaticReaction r : reactions) {
            if (r.getReactionId() == id) {
                return r;
            }
        }
        return null;
    }

    public Boolean addReaction(AutomaticReaction reaction) {
        try {
            reactions.add(reaction);}
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean deleteReaction(int id) {
        for (AutomaticReaction r : reactions) {
            if (r.getReactionId() == id) {
                try {
                    reactions.remove(r);}
                catch (Exception e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}