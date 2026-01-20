package pl.szebi.optimization;

import pl.szebi.sterowanie.AutomationPlanManager;

import java.util.*;

public class CostReductionStrategy extends OptimizationStrategy {

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data, List<AutomationRule> currentRules) {
        if (data == null || data.getForecastConsumed() == null || data.getForecastConsumed().isEmpty()) {
            return false;
        }

        List<AutomationRule> dbRules = loadRulesFromDatabase();
        if (dbRules.isEmpty()) return false;

        List<Double> consumption = data.getForecastConsumed();
        List<Double> production = data.getForecastGenerated();

        int bestWindowIndex = -1;
        double maxBalance = -Double.MAX_VALUE;

        int iterations = Math.min(consumption.size(), production.size());
        for (int i = 0; i < iterations; i++) {
            double balance = production.get(i) - consumption.get(i);
            if (balance > maxBalance) {
                maxBalance = balance;
                bestWindowIndex = i;
            }
        }

        if (bestWindowIndex == -1) return false;
        String bestTimeWindowStr = getWindowString(data.getTimestamp(), bestWindowIndex);

        List<AutomationRule> optimizedRules = new ArrayList<>();
        double totalSavings = 0.0;

        // 2. Przeanalizuj i przesuń plany
        for (AutomationRule rule : dbRules) {
            Map<String, Float> states = rule.getStates();

            if (states.containsKey("power") && states.get("power") >= 1.0f) {

                AutomationRule optimizedRule = new AutomationRule();
                optimizedRule.setDeviceId(rule.getDeviceId());

                Map<String, Float> newStates = new HashMap<>(states);

                if (maxBalance > 0 && newStates.containsKey("temp")) {
                    float currentTemp = newStates.get("temp");
                    if (currentTemp < 24.0f) {
                        newStates.put("temp", currentTemp + 1.0f);
                    }
                }

                optimizedRule.setStates(newStates);
                optimizedRule.setTimeWindow(bestTimeWindowStr);

                optimizedRules.add(optimizedRule);

                String currentWindow = rule.getTimeWindow();
                if (currentWindow == null || !currentWindow.equals(bestTimeWindowStr)) {
                    totalSavings += 1.5;
                }
            }
        }

        plan.setRules(optimizedRules);
        plan.setCostSavings(totalSavings);

        List<pl.szebi.sterowanie.AutomationRule> rulesSterowanie = new ArrayList<>();
        for (AutomationRule rule : optimizedRules) {
            rulesSterowanie.add(rule.convertAutomationRule(rule));
        }
        AutomationPlanManager.applyModifications(rulesSterowanie,0);

        return true;
    }
}