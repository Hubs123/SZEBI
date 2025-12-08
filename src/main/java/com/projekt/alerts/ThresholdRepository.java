package com.projekt.alerts;

import java.util.List;

public class ThresholdRepository {
    private List<Threshold> thresholds;

    public ThresholdRepository() {
    }

    public List<Threshold> getAllThresholds() {
        return thresholds;
    }

    public Threshold getThresholdById(int id) {
        for (Threshold t : thresholds) {
            if (t.getThresholdId() == id) {
                return t;
            }
        }
        return null;
    }

    public Boolean addThreshold(Threshold threshold) {
        try {
        thresholds.add(threshold);}
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean deleteThreshold(int id) {
        for (Threshold t : thresholds) {
            if (t.getThresholdId() == id) {
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
