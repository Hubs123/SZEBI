package com.projekt.optimization;

import java.util.*;

public class CostReductionStrategy extends OptimizationStrategy {
    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data) {
        if (data == null || data.getForecastConsumed() == null || data.getForecastConsumed().size() < 24
                || data.getForecastGenerated() == null || data.getForecastGenerated().size() < 24) {
            return false;
        }

        List<Float> consumed = data.getForecastConsumed();
        List<Float> generated = data.getForecastGenerated();

        // 1. Znalezienie najtańszego okna czasowego
        Map<String, String> lowestCostInfo = findLowestCostWindow(consumed, generated);

        if (lowestCostInfo == null || !lowestCostInfo.containsKey("timeWindow") || !lowestCostInfo.containsKey("minCost")) {
            return false;
        }

        String lowestCostWindow = lowestCostInfo.get("timeWindow");
        float minCost = Float.parseFloat(lowestCostInfo.get("minCost"));

        double costSavings = 0.0;
        //Zapisane zoptymalizowane nastawy urządzeń
        List<AutomationRule> calculatedRules = new ArrayList<>();
        //Nastawy pobrane z bazy
        List<AutomationRule> rules = getRulesFromDatabase();

        // Parsowanie nowego okna czasowego (jedno godzinne)
        List<Integer> newWindow = parseOffTimeWindow(lowestCostWindow);
        if (newWindow == null) return false;

        //generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            Map<String, Float> states = rule.getStates();
            if (states.get("power") == null) {
                return false;
            }
            if (states.get("power") == 0.0f) {
                calculatedRules.add(rule);
                continue;
            }

            // Optymalizacja dla uruchomionych urządzeń (zakładamy, że power == 1.0f oznacza działanie)
            if (states.get("power") == 1.0f) {
                List<Integer> timeWindowOld = parseOffTimeWindow(rule.getTimeWindow());

                if (timeWindowOld == null || timeWindowOld.size() < 2) {
                    calculatedRules.add(rule);
                    continue;
                }

                // Sprawdzamy, czy urządzenie jest już w najlepszym oknie
                if (timeWindowOld.get(0).equals(newWindow.get(0))) {
                    continue;
                }

                // Założenia:
                // 1. Urządzenie działa przez 1 godzinę.
                // 2. Koszt = (Consumed - Generated) + moc urządzenia.

                // Obliczamy koszt dla starego okna
                // Wartość bilansu dla starej godziny (jeśli to jedno godzinne okno)
                int oldStartHour = timeWindowOld.get(0);

                // Zabezpieczenie indeksu
                if (oldStartHour < 0 || oldStartHour >= consumed.size()) {
                    calculatedRules.add(rule);
                    continue;
                }

                // Koszt (bilans) starego i nowego przedziału
                Float costBeforeOptimization = consumed.get(oldStartHour) - generated.get(oldStartHour);
                Float costAfterOptimization = minCost;

                // Jeśli opłaca się przenieść
                if (costAfterOptimization < costBeforeOptimization) {
                    // Używamy nazwy zmiennej z lokalnego scope
                    rule.setTimeWindow(lowestCostWindow);
                    // Obliczamy oszczędność
                    costSavings += savingsCost(costBeforeOptimization, costAfterOptimization);
                    // Dodajemy do zoptymalizowanych nastaw
                    calculatedRules.add(rule);
                }
            }
        }

        //zapisanie wyniku
        plan.setRules(calculatedRules);
        plan.setCostSavings(costSavings);
        return true;
    }

    /**
     * Znajduje jedno-godzinne okno czasowe o najniższym koszcie energetycznym.
     * Cost = Consumed - Generated. Im niższa wartość, tym lepiej (mniej kupujemy/więcej sprzedajemy).
     */
    private Map<String, String> findLowestCostWindow(List<Float> consumed, List<Float> generated) {
        if (consumed.size() != 24 || generated.size() != 24) {
            return null;
        }

        // minCost to Net Load dla danej godziny
        int minIndex = 0;
        float minCost = consumed.get(0) - generated.get(0);
        // Iteracja przez wszystkie 24 godziny
        for (int i = 1; i < consumed.size(); i++) {
            // TUTAJ NASTĘPUJE OBLICZENIE KOSZTU (BILANSU)
            float currentCost = consumed.get(i) - generated.get(i);

            if (currentCost < minCost) {
                minCost = currentCost;
                minIndex = i;
            }
        }

        // Jednogodzinne okno kończy się o następnej pełnej godzinie
        int offPeakStartHour = minIndex;
        int offPeakEndHour = (minIndex + 1) % 24;

        // Zabezpieczenie na wypadek minIndex=23, gdzie (23+1)%24 = 0 (00:00)
        String timeWindowStr = String.format("%02d:00-%02d:00", offPeakStartHour, offPeakEndHour == 0 ? 24 : offPeakEndHour);

        HashMap<String, String> lowestCostInfo = new HashMap<>();
        lowestCostInfo.put("minCost", String.valueOf(minCost));
        lowestCostInfo.put("timeWindow", timeWindowStr);

        return lowestCostInfo;
    }

    private Float savingsCost(Float beforeOptimization, Float afterOptimization) {
        return beforeOptimization - afterOptimization;
    }
}