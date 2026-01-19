package pl.szebi.optimization;

//import pl.szebi.sterowanie.AutomationPlanManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Co2ReductionStrategy extends OptimizationStrategy {
    // Uproszczona stała wartość zaoszczędzonej emisji CO2 przy zastosowaniu optymalizacji
    private static final double DEFAULT_CO2_SAVINGS_PER_SHIFT = 0.5;

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data, List<AutomationRule> currentRules) {
        if (data == null || data.getForecastGenerated() == null || data.getForecastGenerated().size() < 24) {
            return false;
        }
        List<Double> generated = data.getForecastGenerated();

        // okno czasowe dot. przedziału największej generacji
        String peakGenerateWindow = findMaxWindow(generated);

        List<AutomationRule> calculatedRules = new ArrayList<>();

        // Klonowanie reguł, aby nie modyfikować oryginalnej listy
        List<AutomationRule> rules = currentRules.stream()
                .map(AutomationRule::clone)
                .toList();

        double co2Savings = 0.0;

        List<Integer> timeWindowNew = parseOffTimeWindow(peakGenerateWindow);
        if (timeWindowNew.get(0) == -1) return false;

        // generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            Map<String, Float> states = rule.getStates();
            Float power = states.get("power");
            if (power == null) {
                return false;
            }

            // Pomijamy urządzenia wyłączone
            if (power == 0.0f) {
                calculatedRules.add(rule);
                continue;
            }

            // Optymalizacja dla uruchomionych urządzeń
            if (power >= 1.0f) {
                List<Integer> timeWindowOld = parseOffTimeWindow(rule.getTimeWindow());

                if (timeWindowOld.get(0) == -1) {
                    continue;
                }

                // Sprawdzamy, czy urządzenie jest już w najlepszym oknie
                if (timeWindowNew.get(0).equals(timeWindowOld.get(0))) {
                    continue;
                }

                // Założenie: Przenosimy urządzenie do okna z maksymalną generacją
                rule.setTimeWindow(peakGenerateWindow);
                calculatedRules.add(rule);
                co2Savings += DEFAULT_CO2_SAVINGS_PER_SHIFT;
            }
        }

        //zapisanie wyniku
        plan.setRules(calculatedRules);
        plan.setCo2Savings(co2Savings);
//        List<pl.szebi.sterowanie.AutomationRule> rulesSterowanie = new ArrayList<>();
//        for (AutomationRule rule : calculatedRules) {
//            rulesSterowanie.add(rule.convertAutomationRule(rule));
//        }
//        AutomationPlanManager.applyModifications(rulesSterowanie,0);
        return true;
    }
}