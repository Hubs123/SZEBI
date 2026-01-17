package pl.szebi.alerts;

import java.util.ArrayList;
import java.util.List;
import pl.szebi.sterowanie.Device;
import pl.szebi.sterowanie.DeviceRepository;

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

    public List<Threshold> getAllThresholds() {
        return thresholds;
    }

    public List<AutomaticReaction> getAllReactions() {
        return reactions;
    }

    public List<Device> getDevices() { return devices; }

    public Boolean addThreshold(Threshold threshold) {
        try {
            thresholds.add(threshold);
        } catch (Exception e) {
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

    public Boolean addDevice(Device device) {
        try {
            devices.add(device);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Device getDeviceById(Integer id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    public Threshold getThresholdById(Integer id) {
        for (Threshold threshold : thresholds) {
            if (threshold.getId().equals(id)) {
                return threshold;
            }
        }
        return null;
    }

    public AutomaticReaction getReactionById(Integer id) {
        for (AutomaticReaction reaction : reactions) {
            if (reaction.getId().equals(id)) {
                return reaction;
            }
        }
        return null;
    }

    public Boolean modifyThreshold(Threshold threshold, Float valueWarning, Float valueEmergency) {
        try {
            threshold.setValues(valueWarning, valueEmergency);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}