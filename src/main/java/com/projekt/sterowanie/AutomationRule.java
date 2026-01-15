package com.projekt.sterowanie;

import java.util.HashMap;
import java.util.Map;

public class AutomationRule {
    private Integer deviceId;
    private Map<String, Float> states = new HashMap<>();
    private String timeWindow;

    public AutomationRule(Integer deviceId, Map<String, Float> states, String timeWindow) {
        this.deviceId = deviceId;
        if (states != null) this.states.putAll(states);
        this.timeWindow = timeWindow;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public Map<String, Float> getStates() {
        return Map.copyOf(states);
    }

    public String getTimeWindow() {
        return timeWindow;
    }
}
