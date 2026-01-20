package pl.szebi.optimization;

import java.util.Map;

public class AutomationRule {
    // Pola nie mogą być final, aby Jackson mógł je ustawić
    private Integer deviceId;
    private Map<String, Float> states;
    private String timeWindow;

    // 1. WYMAGANY: Pusty konstruktor dla Jacksona
    public AutomationRule() {
    }

    // Konstruktor, którego używasz w logice biznesowej
    public AutomationRule(Integer deviceId, Map<String, Float> states) {
        this.deviceId = deviceId;
        this.states = states;
    }

    public AutomationRule(Integer deviceId, Map<String, Float> states, String timeWindow) {
        this.deviceId = deviceId;
        this.states = states;
        this.timeWindow = timeWindow;
    }

    // 2. Metoda clone (była wcześniej, zostawiamy)
    @Override
    public AutomationRule clone() {
        return new AutomationRule(this.deviceId, this.states, this.timeWindow);
    }

    // GETTERY
    public Integer getDeviceId() {
        return deviceId;
    }

    public Map<String, Float> getStates() {
        return states;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    // 3. WYMAGANE: Settery dla Jacksona
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public void setStates(Map<String, Float> states) {
        this.states = states;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    @Override
    public String toString() {
        return "Rule{dev=" + deviceId + ", win='" + timeWindow + "'}";
    }
}