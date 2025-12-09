package com.projekt.alerts;

import java.util.List;

public class ThresholdRepository {
    private List<Threshold> thresholds;

    public List<Threshold> getAll() {
        return thresholds;
    }

    public Threshold getById(int id) {
        for (Threshold t : thresholds) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public Boolean add(Threshold threshold) {
        try {
        thresholds.add(threshold);}
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean delete(int id) {
        for (Threshold t : thresholds) {
            if (t.getId() == id) {
                try {thresholds.remove(t);}
                catch (Exception e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
