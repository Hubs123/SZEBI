package pl.szebi.optimization;

import pl.szebi.sterowanie.AutomationPlanManager;

import java.util.*;

public class LoadReductionStrategy extends OptimizationStrategy {

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data, List<AutomationRule> currentRules) {
        if (data == null || data.getForecastConsumed() == null || data.getForecastConsumed().isEmpty()) {
            return false;
        }

        List<AutomationRule> dbRules = loadRulesFromDatabase();
        if (dbRules.isEmpty()) return false;

        List<Double> consumption = data.getForecastConsumed();

        // 1. Znajdź okno z NAJMNIEJSZYM obciążeniem sieci
        int bestWindowIndex = -1;
        double minLoad = Double.MAX_VALUE;

        for (int i = 0; i < consumption.size(); i++) {
            if (consumption.get(i) < minLoad) {
                minLoad = consumption.get(i);
                bestWindowIndex = i;
            }
        }

        if (bestWindowIndex == -1) return false;
        String bestTimeWindowStr = getWindowString(data.getTimestamp(), bestWindowIndex);

        List<AutomationRule> optimizedRules = new ArrayList<>();
        double estimatedPeakShaving = 0.0;

        // 2. Przesuń energochłonne zadania do doliny
        for (AutomationRule rule : dbRules) {
            Map<String, Float> states = rule.getStates();

            if (states.containsKey("power") && states.get("power") >= 1.0f) {

                AutomationRule optimizedRule = new AutomationRule();
                optimizedRule.setDeviceId(rule.getDeviceId());

                Map<String, Float> newStates = new HashMap<>(states);
                optimizedRule.setStates(newStates);

                // Ustawiamy czas na moment najniższego obciążenia sieci
                optimizedRule.setTimeWindow(bestTimeWindowStr);

                optimizedRules.add(optimizedRule);

                String currentWindow = rule.getTimeWindow();
                if (currentWindow == null || !currentWindow.equals(bestTimeWindowStr)) {
                    estimatedPeakShaving += 0.5;
                }
            }
        }

        plan.setRules(optimizedRules);
        plan.setCostSavings(estimatedPeakShaving);

        List<pl.szebi.sterowanie.AutomationRule> rulesSterowanie = new ArrayList<>();
        for (AutomationRule rule : optimizedRules) {
            rulesSterowanie.add(rule.convertAutomationRule(rule));
        }
        AutomationPlanManager.applyModifications(rulesSterowanie,0);

        return true;
    }
}