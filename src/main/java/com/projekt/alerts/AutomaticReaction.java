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
                if (DeviceManager.deviceRepo.findById(deviceId).getStates().get("power") == 0.0f) {
                    break;
                }
                if (DeviceManager.deviceRepo.findById(deviceId).getStates().get("power") == null) {
                    // dodać tutaj błąd że device nie ma stanu power co w założeniach każde miało mieć
                    break;
                }
                Map<String, Float> offset1 = Map.of("power", -1.0f);
                AutomationRule reaction1 = new AutomationRule(deviceId, offset1, "placeholder");
                AutomationPlanManager.applyModifications(List.of(reaction1), 1);
                break;
            case "turnOn":
                if (DeviceManager.deviceRepo.findById(deviceId).getStates().get("power") == 1.0f) {
                    break;
                }
                if (DeviceManager.deviceRepo.findById(deviceId).getStates().get("power") == null) {
                    // dodać tutaj błąd że device nie ma stanu power co w założeniach każde miało mieć
                    break;
                }
                Map<String, Float> offset2 = Map.of("power", 1.0f);
                AutomationRule reaction2 = new AutomationRule(deviceId, offset2, "placeholder");
                AutomationPlanManager.applyModifications(List.of(reaction2), 1);
                break;
            default:
                break;
        }
    }
}
