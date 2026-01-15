package com.projekt.alerts;

import com.projekt.sterowanie.AutomationPlanManager;
import com.projekt.sterowanie.AutomationRule;
import com.projekt.sterowanie.DeviceManager;

import java.util.List;
import java.util.Map;

public class AutomaticReaction {
    private final Integer id;
    private String name;

    public AutomaticReaction(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void executeReaction(Integer deviceId) {
        switch (name) {
            case "turnOff":
                if (!DeviceManager.getDevice(deviceId).isOn()) {
                    break;
                }
                Map<String, Float> offState = Map.of("power", 0.0f);
                AutomationRule reaction1 = new AutomationRule(deviceId, offState);
                AutomationPlanManager.applyModifications(List.of(reaction1), 1);
                break;
            case "turnOn":
                if (DeviceManager.getDevice(deviceId).isOn()) {
                    break;
                }
                Map<String, Float> onState = Map.of("power", 1.0f);
                AutomationRule reaction2 = new AutomationRule(deviceId, onState);
                AutomationPlanManager.applyModifications(List.of(reaction2), 1);
                break;
            default:
                break;
        }
    }
}
