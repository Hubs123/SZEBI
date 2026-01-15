package com.projekt.sterowanie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutomationPlanManager {
    private final AutomationPlanRepository planRepo = new AutomationPlanRepository();
    private static volatile AutomationPlan currentPlan = null;

    public Integer createPlan(String name, List<AutomationRule> rules) {
        if (rules == null) return null;
        AutomationPlan plan = new AutomationPlan(name, rules);
        boolean added = planRepo.add(plan);
        return added ? plan.getId() : null;
    }

    public Boolean removePlan(Integer planId) {
        return planRepo.delete(planId);
    }

    public AutomationPlan getCurrentPlan() {
        return currentPlan;
    }

    public Boolean activatePlan(Integer planId) {
        AutomationPlan plan = planRepo.findById(planId);
        if (plan == null) return false;
        List<AutomationRule> rules = plan.getRules();
        List<Pair<Integer, Map<String, Float>>> devicesStates = new ArrayList<>();
        for (AutomationRule rule : rules) {
            devicesStates.add(new Pair<>(rule.getDeviceId(), rule.getStates()));
        }
        Boolean commandApplied = DeviceManager.applyCommands(devicesStates);
        if (commandApplied) {
            currentPlan = plan;
        }
        else {
            currentPlan = null;
        }
        return commandApplied;
    }

    static public Boolean applyModifications(List<AutomationRule> rules, Integer priority) {
        final boolean emergency = priority > 0;
        List<Pair<Integer, Map<String, Float>>> devicesStates = new ArrayList<>();
        for (AutomationRule rule : rules) {
            devicesStates.add(new Pair<>(rule.getDeviceId(), rule.getStates()));
        }
        if (currentPlan == null || currentPlan.getName().equals("temp")) {
            AutomationPlan tempPlan = new AutomationPlan("temp", rules);
            if (DeviceManager.applyCommands(devicesStates, emergency)) {
                currentPlan = tempPlan;
                if (emergency) {
                    for (AutomationRule rule : rules) {
                        Device d = DeviceManager.getDevice(rule.getDeviceId());
                        if (d != null) d.emergencyLock();
                    }
                }
                return true;
            }
            // tymczasowy plan utworzony na bazie poleceń od modułu optymalizacji
        }
        boolean commandApplied = DeviceManager.applyCommands(devicesStates);
        if (commandApplied && emergency) {
            for (AutomationRule rule : rules) {
                Device d = DeviceManager.getDevice(rule.getDeviceId());
                if (d != null) d.emergencyLock();
            }
        }
        return commandApplied;
    }

    public Boolean saveToDatabase(AutomationPlan plan) {
        return planRepo.save(plan);
    }
}
