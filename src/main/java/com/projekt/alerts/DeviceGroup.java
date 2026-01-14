package com.projekt.alerts;

import java.util.ArrayList;
import java.util.List;
import com.projekt.sterowanie.Device;
import com.projekt.sterowanie.DeviceRepository;

public class DeviceGroup {
    private final Integer id;
    private String groupName;
    private List<Threshold> thresholds;
    private List<AutomaticReaction> reactions;
    private List<Device> devices;

    public DeviceGroup(Integer id, String groupName, List<Threshold> thresholds, List<AutomaticReaction> reactions) {
        this.id = id;
        this.groupName = groupName;
        this.thresholds = (thresholds != null) ? thresholds : new ArrayList<>();
        this.reactions = (reactions != null) ? reactions : new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Threshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<Threshold> thresholds) {
        this.thresholds = thresholds;
    }

    public List<AutomaticReaction> getReactions() {
        return reactions;
    }

    public List<Device> getDevices() { return devices; }

    public void setReactions(List<AutomaticReaction> reactions) {
        this.reactions = reactions;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public Boolean addThreshold(Threshold threshold) {
        try {
            thresholds.add(threshold);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public Boolean removeThreshold(Threshold threshold) {
        try {
            thresholds.remove(threshold);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

        public Boolean addReaction(AutomaticReaction reaction) {
        try {
            reactions.add(reaction);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public Boolean removeReaction(AutomaticReaction reaction) {
        try {
            reactions.remove(reaction);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
}