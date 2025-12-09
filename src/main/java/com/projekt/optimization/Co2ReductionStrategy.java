package com.projekt.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Co2ReductionStrategy extends OptimizationStrategy {
    //Uproszczona stała wartość zaoszczędzonej emisji CO2 przy zastosowaniu optymalizacji
    private static final double DEFAULT_CO2_SAVINGS_PER_SHIFT = 0.5;

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data) {
        if (data == null || data.getForecastGenerated() == null || data.getForecastGenerated().size() < 24) {
            return false;
        }
        List<Float> generated = data.getForecastGenerated();

        //okna czasowe dotyczace przedzialow zuzycia
        String peakGenerateWindow = findMaxWindow(generated);

        List<AutomationRule> calculatedRules = new ArrayList<>();
        List<AutomationRule> rules = getRulesFromDatabase();

        double co2Savings = 0.0;
        //generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            Map<String, Float> states = rule.getStates();
            if (states.get("power") == null) {
                return false;
            }
            if (states.get("power") == 0.0f) {
                continue;
            }
            //Optymalizacja dla uruchomionych urządzeń
            if (states.get("power") == 1.0f) {
                List<Integer> timeWindowOld = parseOffTimeWindow(rule.getTimeWindow());
                List<Integer> timeWindowNew = parseOffTimeWindow(peakGenerateWindow);
                if (timeWindowNew.get(0) <= timeWindowOld.get(0) || timeWindowOld.get(1) <= timeWindowNew.get(1)) {
                    continue;
                }
                rule.setTimeWindow(peakGenerateWindow);
                calculatedRules.add(rule);
                co2Savings += DEFAULT_CO2_SAVINGS_PER_SHIFT;
            }
        }

        //zapisanie wyniku
        plan.setRules(calculatedRules);
        return true;
    }
}