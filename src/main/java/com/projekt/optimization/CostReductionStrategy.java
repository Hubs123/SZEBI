package com.projekt.optimization;

import java.util.*;

public class CostReductionStrategy extends OptimizationStrategy {

    // Uproszczone założenie dla mocy urządzenia, używane do obliczenia oszczędności
    private static final float DEFAULT_DEVICE_POWER = 1.0f;

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data) {
        if (data == null || data.getForecastConsumed() == null || data.getForecastConsumed().size() < 24
                || data.getForecastGenerated() == null || data.getForecastGenerated().size() < 24) {
            return false;
        }

        List<Float> consumed = data.getForecastConsumed();
        List<Float> generated = data.getForecastGenerated();

        // 1. Znalezienie najtańszego JEDNOGODZINNEGO okna czasowego (Net Load)
        Map<String, String> lowestCostInfo = findLowestCostWindow(consumed, generated);

        if (lowestCostInfo == null || !lowestCostInfo.containsKey("timeWindow") || !lowestCostInfo.containsKey("minCost")) {
            return false;
        }

        String lowestCostWindow = lowestCostInfo.get("timeWindow");
        Float minNetLoad = Float.parseFloat(lowestCostInfo.get("minCost"));

        List<AutomationRule> calculatedRules = new ArrayList<>();

        Double costSavings = 0.0;
        List<AutomationRule> rules = getRulesFromDatabase();

        // Parsowanie nowego okna czasowego (jedno godzinne)
        List<Integer> newWindow = parseOffTimeWindow(lowestCostWindow);
        if (newWindow == null) return false;

        //generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            Map<String, Float> states = rule.getStates();
            if (states.get("power") == null) {
                // Ta reguła powinna prowadzić do błędu lub pominięcia, ale trzymamy się struktury
                return false;
            }
            if (states.get("power") == 0.0f) {
                calculatedRules.add(rule); // Zachowaj wyłączone urządzenia
                continue;
            }

            // Optymalizacja dla uruchomionych urządzeń (zakładamy, że power == 1.0f oznacza działanie)
            if (states.get("power") == 1.0f) {
                List<Integer> timeWindowOld = parseOffTimeWindow(rule.getTimeWindow());

                if (timeWindowOld == null || timeWindowOld.size() < 2) {
                    calculatedRules.add(rule);
                    continue; // Pomiń, jeśli nie da się sparsować
                }

                // Sprawdzamy, czy urządzenie jest już w najlepszym oknie
                if (timeWindowOld.get(0) == newWindow.get(0)) {
                    calculatedRules.add(rule);
                    continue;
                }

                // Założenia:
                // 1. Urządzenie działa przez 1 godzinę.
                // 2. Koszt = Net Load (Consumed - Generated) + moc urządzenia.

                // Obliczamy koszt dla starego okna
                // Wartość bilansu dla starej godziny (jeśli to jedno godzinne okno)
                int oldStartHour = timeWindowOld.get(0);

                // Zabezpieczenie indeksu
                if (oldStartHour < 0 || oldStartHour >= consumed.size()) {
                    calculatedRules.add(rule);
                    continue;
                }

                Float oldNetLoad = consumed.get(oldStartHour) - generated.get(oldStartHour);

                // Koszt (bilans) starego i nowego przedziału + moc urządzenia (1.0f)
                Float costBeforeOptimization = oldNetLoad + DEFAULT_DEVICE_POWER;
                Float costAfterOptimization = minNetLoad + DEFAULT_DEVICE_POWER;

                // Jeśli opłaca się przenieść
                if (costAfterOptimization < costBeforeOptimization) {

                    // Używamy nazwy zmiennej z lokalnego scope
                    rule.setTimeWindow(lowestCostWindow);

                    // Obliczamy oszczędność
                    costSavings += savingsCost(costBeforeOptimization, costAfterOptimization);

                    calculatedRules.add(rule);
                } else {
                    calculatedRules.add(rule); // Jeśli się nie opłaca, zachowujemy stary
                }
            } else {
                calculatedRules.add(rule); // Dodajemy pozostałe reguły
            }
        }

        //zapisanie wyniku
        plan.setRules(calculatedRules);
        plan.setCostSavings(costSavings);
        return true;
    }

    /**
     * Znajduje jedno-godzinne okno czasowe o najniższym bilansie energetycznym (Net Load).
     * Net Load = Consumed - Generated. Im niższa wartość, tym lepiej (mniej kupujemy/więcej sprzedajemy).
     */
    private Map<String, String> findLowestCostWindow(List<Float> consumed, List<Float> generated) {
        if (consumed.size() != 24 || generated.size() != 24) {
            return null;
        }

        // minCost to Net Load dla danej godziny
        Float minNetLoad = Float.MAX_VALUE;
        Integer minIndex = -1;

        // Iteracja przez wszystkie 24 godziny
        for (int i = 0; i < consumed.size(); i++) {
            // TUTAJ NASTĘPUJE OBLICZENIE KOSZTU (BILANSU)
            Float currentNetLoad = consumed.get(i) - generated.get(i);

            if (currentNetLoad < minNetLoad) {
                minNetLoad = currentNetLoad;
                minIndex = i;
            }
        }

        if (minIndex == -1) {
            return null;
        }

        int offPeakStartHour = minIndex;
        // Jednogodzinne okno kończy się o następnej pełnej godzinie
        int offPeakEndHour = (minIndex + 1) % 24;

        // Zabezpieczenie na wypadek minIndex=23, gdzie (23+1)%24 = 0 (00:00)
        String timeWindowStr = String.format("%02d:00-%02d:00", offPeakStartHour, offPeakEndHour == 0 ? 24 : offPeakEndHour);

        HashMap<String, String> lowestCostInfo = new HashMap<>();
        lowestCostInfo.put("minCost", String.valueOf(minNetLoad));
        lowestCostInfo.put("timeWindow", timeWindowStr);

        return lowestCostInfo;
    }

    private Float savingsCost(Float beforeOptimization, Float afterOptimization) {
        return beforeOptimization - afterOptimization;
    }
}