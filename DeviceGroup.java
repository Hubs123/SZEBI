import java.util.List;

public class DeviceGroup {
    private final Integer deviceGroupId;
    private String name;
    private List<Threshold> thresholds;
    private List<AutomaticReaction> reactions;

    public DeviceGroup(Integer deviceGroupId, String name, List<Threshold> thresholds, List<AutomaticReaction> reactions) {
        this.deviceGroupId = deviceGroupId;
        this.name = name;
        this.thresholds = thresholds;
        this.reactions = reactions;
    }

    public Integer getDeviceGroupId() {
        return deviceGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setReactions(List<AutomaticReaction> reactions) {
        this.reactions = reactions;
    }
}
