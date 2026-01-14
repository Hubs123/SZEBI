package com.projekt.optimization;

import java.util.*;
import java.util.stream.Collectors;
//import com.projekt.sterowanie.AutomationPlanManager;

public class LoadReductionStrategy extends OptimizationStrategy {

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data, List<AutomationRule> currentRules) {
        if (data == null || data.getForecastConsumed() == null || data.getForecastConsumed().size() < 24) {
            return false;
        }

        List<Float> consumed = data.getForecastConsumed();

        // Okno czasowe dot. przedziału najmniejszego zużycia
        String offPeakLoadWindow = findMinWindow(consumed);

        List<AutomationRule> calculatedRules = new ArrayList<>();

        // Klonowanie reguł, aby nie modyfikować oryginalnej listy
        List<AutomationRule> rules = currentRules.stream()
                .map(AutomationRule::clone)
                .toList();

        List<Integer> timeWindowNew = parseOffTimeWindow(offPeakLoadWindow);
        if (timeWindowNew.get(0) == -1) return false;

        //generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            Map<String, Float> states = rule.getStates();
            Float power = states.get("power");
            if (power == null) {
                return false;
            }

            // Pomijamy urządzenia wyłączone
            if (power == 0.0f) {
                continue;
            }

            // Optymalizacja dla uruchomionych urządzeń
            if (power >= 1.0f) {
                List<Integer> timeWindowOld = parseOffTimeWindow(rule.getTimeWindow());

                if (timeWindowOld.get(0) == -1) {
                    continue;
                }

                if (timeWindowNew.get(0).equals(timeWindowOld.get(0))) {
                    continue;
                }

                rule.setTimeWindow(offPeakLoadWindow);
                calculatedRules.add(rule);

            }
        }

        //zapisanie wyniku
        plan.setRules(calculatedRules);

//        List<com.projekt.sterowanie.AutomationRule> rulesSterowanie = new ArrayList<>();
//        for (AutomationRule rule : calculatedRules) {
//            rulesSterowanie.add(rule.convertAutomationRule(rule));
//        }
//        AutomationPlanManager.applyModifications(rulesSterowanie,0);
        return true;
    }
}