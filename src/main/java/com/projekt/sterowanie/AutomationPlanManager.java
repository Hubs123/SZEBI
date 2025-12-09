package com.projekt.sterowanie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutomationPlanManager {
    private final AutomationPlanRepository planRepo = new AutomationPlanRepository();
    private static AutomationPlan currentPlan = null;
    private static Boolean isCurrentPlanPrio = false;

    public Integer createPlan(String name, List<AutomationRule> rules) {
        if (rules == null) return null;
        AutomationPlan plan = new AutomationPlan(name, rules);
        boolean added = planRepo.add(plan);
        return added ? plan.getId() : null;
    }

    public Boolean removePlan(Integer planId) {
        return planRepo.delete(planId);
    }

    public Boolean getCurrentPlanPrio() {
        return isCurrentPlanPrio;
    }

    public AutomationPlan getCurrentPlan() {
        return currentPlan;
    }

    public Boolean activatePlan(Integer planId) {
        AutomationPlan plan = planRepo.findById(planId);
        if (plan == null) return false;
        List<AutomationRule> rules = plan.getRules();
        List<Device> devices = new ArrayList<>();
        for (AutomationRule rule : rules) {
            devices.add(DeviceManager.deviceRepo.findById(rule.getDeviceId()));
        }
        boolean commandApplied = true;
        for (int i = 0; i < devices.size(); i++) {
            if (!devices.get(i).applyCommand(rules.get(i).getStates())) {
                commandApplied = false;
                // podobnie jak w device manager
            }
        }
        if (commandApplied) {
            currentPlan = plan;
        }
        else {
            currentPlan = null;
            // na razie bez rollbacku
        }
        return commandApplied;
        // time window na razie nie zaimplementowany - bardzo skomplikowane

    }

    static public Boolean applyModifications(List<AutomationRule> offsets, Integer priority) {
        List<Device> devices = new ArrayList<>();
        for (AutomationRule rule : offsets) {
            devices.add(DeviceManager.deviceRepo.findById(rule.getDeviceId()));
        }
        if (currentPlan == null || currentPlan.getName().equals("temp")) {
            List<AutomationRule> rules = new ArrayList<>();
            for (int i = 0; i < devices.size(); i++) {
                Device device = devices.get(i);
                Map<String, Float> newStates = device.getStates();
                offsets.get(i).getStates().forEach((key, value) ->
                        newStates.merge(key, value, Float::sum)
                ); // dodanie wszystkich wartości offsetów stanów
                AutomationRule rule = new AutomationRule(device.getId(), newStates, offsets.get(i).getTimeWindow());
                rules.add(rule);
            }
            AutomationPlan tempPlan = new AutomationPlan("temp", rules);
            boolean commandApplied = true;
            for (int i = 0; i < devices.size(); i++) {
                if(!devices.get(i).applyCommand(rules.get(i).getStates())) {
                    commandApplied = false;
                }
            }
            if (commandApplied) {
                currentPlan = tempPlan;
                return true;
            }
            // tymczasowy plan utworzony na bazie obecnych stanów i modyfikacji od modułu optymalizacji
        }
        boolean commandApplied = true;
        for (int i = 0; i < offsets.size(); i++) {
            Map<String, Float> oldStates = devices.get(i).getStates();
            Map<String, Float> newStates = offsets.get(i).getStates();
            oldStates.forEach((key, value) ->
                    newStates.merge(key, value, Float::sum)
            );
            if (!devices.get(i).applyCommand(newStates)) {
                commandApplied = false;
            }
        }
        // priority 0 - zwykłe, 1 - priorytetowe
        isCurrentPlanPrio = priority > 0;
        return commandApplied;
    }

    public Boolean saveToDatabase(AutomationPlan plan) {
        return planRepo.save(plan);
    }
}
