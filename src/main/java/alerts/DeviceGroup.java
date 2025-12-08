package alerts;

import java.util.List;

public class DeviceGroup {
    private final Integer deviceGroupId;
    private String groupName;
    private List<Threshold> thresholds;
    private List<AutomaticReaction> reactions;
    private List<Device> devices;

    public DeviceGroup(Integer deviceGroupId, String groupName, List<Threshold> thresholds, List<AutomaticReaction> reactions) {
        this.deviceGroupId = deviceGroupId;
        this.groupName = groupName;
        this.thresholds = thresholds;
        this.reactions = reactions;
    }

    public Integer getDeviceGroupId() {
        return deviceGroupId;
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
}