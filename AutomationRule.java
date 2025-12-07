import java.util.Map;

public class AutomationRule {
    private Integer deviceId;
    private Map<String, Float> states;
    private String timeWindow;

    public Integer getDeviceId() {
        return deviceId;
    }

    public Map<String, Float> getStates() {
        return states;
    }

    public String getTimeWindow() {
        return timeWindow;
    }
}
