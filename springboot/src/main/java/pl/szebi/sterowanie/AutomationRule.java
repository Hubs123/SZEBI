package pl.szebi.sterowanie;

import java.util.HashMap;
import java.util.Map;

public class AutomationRule {
    private Integer deviceId;
    private Map<String, Float> states = new HashMap<>();
    private String timeWindow;

    // musi być dla serializacji jackson
    public AutomationRule() {}

    // bez timeWindow - bezterminowo
    public AutomationRule(Integer deviceId, Map<String, Float> states) {
        this.deviceId = deviceId;
        this.states = states;
        this.timeWindow = null;
    }

    // timeWindow np: "12:00-15:00" oznacza wprowadzenie zmian o 12:00 i wycofanie o 15:00
    public AutomationRule(Integer deviceId, Map<String, Float> states, String timeWindow) {
        this.deviceId = deviceId;
        if (states != null) this.states.putAll(states);
        this.timeWindow = timeWindow;
    }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public Map<String, Float> getStates() { return states; }
    public void setStates(Map<String, Float> states) { this.states = states; }

    public String getTimeWindow() { return timeWindow; }
    public void setTimeWindow(String timeWindow) { this.timeWindow = timeWindow; }
}
