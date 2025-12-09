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
                Map<String, Float> offset = Map.of("power", -1.0f);
                AutomationRule reaction = new AutomationRule(deviceId, offset, "placeholder");
                AutomationPlanManager.applyModifications(List.of(reaction), 1);
                break;
            default:
                break;
        }
    }
}
