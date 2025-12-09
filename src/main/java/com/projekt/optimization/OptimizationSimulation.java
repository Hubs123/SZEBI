package com.projekt.optimization;

import java.util.List;
import java.util.stream.Collectors;

public class OptimizationSimulation {

    public static void main(String[] args) {
        System.out.println("--- Symulacja Systemu Optymalizacji Energetycznej ---");

        // 1. Przygotowanie danych
        OptimizationData mockData = OptimizationMockData.generateMockForecastData();
        List<AutomationRule> initialRules = OptimizationMockData.generateMockAutomationRules();
        int userId = 123;

        System.out.println("\n--- 1. Dane Wprowadzające (Prognoza i Reguły Początkowe) ---");
        System.out.printf("   Prognoza Zużycia (max: %.1f, min: %.1f)%n",
                mockData.getForecastConsumed().stream().mapToDouble(f -> f).max().orElse(0),
                mockData.getForecastConsumed().stream().mapToDouble(f -> f).min().orElse(0)
        );
        System.out.printf("   Prognoza Generacji (max: %.1f, min: %.1f)%n",
                mockData.getForecastGenerated().stream().mapToDouble(f -> f).max().orElse(0),
                mockData.getForecastGenerated().stream().mapToDouble(f -> f).min().orElse(0)
        );
        System.out.println("   Reguły Automatyzacji Początkowe (uruchomione urządzenia):");
        initialRules.stream()
                .filter(r -> r.getStates().get("power") >= 1.0f)
                .forEach(r -> System.out.printf("      - Urządzenie %d: (obecnie %s)%n",
                        r.getDeviceId(),
                        r.getTimeWindow()
                ));


        // Inicjalizacja Repozytorium (tylko na potrzeby zapisu)
        OptimizationPlanRepository repository = new OptimizationPlanRepository();

        // 2. Symulacja Strategii Redukcji Kosztów
        simulateStrategy(
                new CostReductionStrategy(),
                "Redukcja Kosztów",
                userId,
                mockData,
                initialRules,
                repository
        );

        // 3. Symulacja Strategii Redukcji Obciążenia
        simulateStrategy(
                new LoadReductionStrategy(),
                "Redukcja Obciążenia",
                userId,
                mockData,
                initialRules,
                repository
        );

        // 4. Symulacja Strategii Redukcji CO2
        simulateStrategy(
                new Co2ReductionStrategy(),
                "Redukcja CO2",
                userId,
                mockData,
                initialRules,
                repository
        );

        System.out.println("\n--- 5. Zapisane Plany Optymalizacji w Repozytorium ---");
        repository.findAll().forEach(plan -> System.out.println("   - " + plan));
    }

    private static void simulateStrategy(
            OptimizationStrategy strategy,
            String strategyName,
            Integer userId,
            OptimizationData data,
            List<AutomationRule> initialRules,
            OptimizationPlanRepository repository
    ) {
        System.out.printf("\n--- 2. Wykonanie Strategii: %s ---%n", strategyName);

        // Tworzymy nowy plan dla każdej symulacji
        OptimizationPlan plan = new OptimizationPlan(userId, strategy);

        // Wymagane jest sklonowanie listy reguł na wejściu,
        // ponieważ strategia modyfikuje reguły wewnątrz
        List<AutomationRule> rulesToOptimize = initialRules.stream()
                .map(AutomationRule::clone)
                .collect(Collectors.toList());

        boolean success = strategy.calculate(plan, data, rulesToOptimize);

        if (success) {
            repository.save(plan);
            System.out.println("   Obliczenia zakończone pomyślnie. Zapisano Plan ID: " + plan.getId());

            String costSavings = plan.getCostSavings() > 0 ? String.format("%.2f jednostek", plan.getCostSavings()) : "N/A";
            String co2Savings = plan.getCo2Savings() > 0 ? String.format("%.2f kg", plan.getCo2Savings()) : "N/A";

            System.out.printf("   Oszczędność Kosztów: %s%n", costSavings);
            System.out.printf("   Oszczędność CO2: %s%n", co2Savings);

            System.out.println("   Zoptymalizowane Reguły (Przesunięte):");
            plan.getRules().stream()
                    .filter(r -> !initialRules.stream().anyMatch(ir -> ir.getDeviceId().equals(r.getDeviceId()) && ir.getTimeWindow().equals(r.getTimeWindow())))
                    .forEach(r -> System.out.printf("      - Urządzenie %d: Nowe okno: %s%n", r.getDeviceId(), r.getTimeWindow()));

        } else {
            System.err.printf("   Błąd w obliczeniach dla strategii %s.%n", strategyName);
        }
    }
}