package com.projekt.alerts;

import java.util.List;

public class AutomaticReactionRepository {
    private List<AutomaticReaction> reactions;

    public List<AutomaticReaction> getAll() {
        return reactions;
    }

    public AutomaticReaction getById(int id) {
        for (AutomaticReaction r : reactions) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public Boolean add(AutomaticReaction reaction) {
        try {
            reactions.add(reaction);}
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean delete(int id) {
        for (AutomaticReaction r : reactions) {
            if (r.getId() == id) {
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