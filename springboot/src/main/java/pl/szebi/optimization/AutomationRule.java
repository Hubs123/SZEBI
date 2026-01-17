package pl.szebi.optimization;

import java.util.Map;

public class AutomationRule {
    private final Integer deviceId;
    private final Map<String, Float> states;
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

    public AutomationRule(Integer deviceId, Map<String, Float> states, String timeWindow) {
        this.deviceId = deviceId;
        this.states = states;
        this.timeWindow = timeWindow;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    // Dodanie metody clone() dla bezpiecznego tworzenia kopii na potrzeby strategii
    public AutomationRule clone() {
        return new AutomationRule(this.deviceId, this.states, this.timeWindow);
    }

//    public pl.szebi.sterowanie.AutomationRule convertAutomationRule(AutomationRule rule) {
//        return new pl.szebi.sterowanie.AutomationRule(rule.getDeviceId(), rule.getStates(), rule.getTimeWindow());
//    }
}