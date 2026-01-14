package com.projekt.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OptimizationMockData {

    public static OptimizationData generateMockForecastData() {
        OptimizationData data = new OptimizationData();
        Date now = new Date();

        // Symulacja 24-godzinnej prognozy zużycia (większe zużycie rano 6-8 i wieczorem 17-19)
        List<Float> consumed = Arrays.asList(0.5f, 0.4f, 0.4f, 0.3f, 0.5f, 1.0f, 2.5f, 3.0f, 2.8f, 2.5f, 2.0f, 1.8f, 1.5f, 1.6f, 1.7f, 2.2f, 3.5f, 4.0f, 3.8f, 2.5f, 1.5f, 1.0f, 0.8f, 0.6f);
        // Symulacja sprzedaży (pomijamy, nieużywane w strategiach)
        List<Float> sold = Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.5f, 1.2f, 1.0f, 0.8f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        // Symulacja stanu magazynowania (pomijamy, nieużywane w strategiach)
        List<Float> stored = Arrays.asList(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f);
        // Symulacja generacji (tylko w słoneczne godziny, max w 11:00-13:00)
        List<Float> generated = Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f, 0.5f, 1.5f, 2.5f, 3.5f, 4.0f, 4.0f, 3.5f, 3.0f, 2.0f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        data.loadForecast(now, consumed, sold, stored, generated);
        return data;
    }

    public static List<Float> generateMockTemperatureData() {
        // Symulacja 24-godzinnej prognozy temperatury (spadek poniżej 20.0°C w godz. 17:00-19:00)
        return Arrays.asList(
                20.5f, 20.6f, 20.7f, 20.7f, 20.6f, 20.5f, 20.4f, 20.3f, 20.2f, 20.1f, 20.0f, 20.0f,
                20.1f, 20.2f, 20.3f, 20.3f, 19.8f, 19.5f, 19.4f, 19.6f, 20.0f, 20.3f, 20.4f, 20.5f
        );
    }

    /**
     * Generuje mockowe reguły automatyzacji dla symulacji.
     * Zakładamy, że urządzenia mają być włączone na 1 godzinę.
     */
    public static List<AutomationRule> generateMockAutomationRules() {
        List<AutomationRule> rules = new ArrayList<>();

        // Urządzenie 1: Pralka (duże zużycie) - obecnie ustawiona na godzinę szczytu 17:00-18:00
        rules.add(new AutomationRule(
                1,
                Map.of("power", 1.0f),
                "17:00-18:00"
        ));

        // Urządzenie 2: Zmywarka (średnie zużycie) - obecnie ustawiona na wczesny ranek 07:00-08:00
        rules.add(new AutomationRule(
                2,
                Map.of("power", 1.0f),
                "07:00-08:00"
        ));

        // Urządzenie 3: Pompa ciepła (wyłączona) - bez optymalizacji
        rules.add(new AutomationRule(
                3,
                Map.of("power", 0.0f),
                "00:00-08:00"
        ));

        // Urządzenie 4: Ładowarka EV (średnie zużycie) - obecnie ustawiona na wczesny ranek 06:00-07:00
        rules.add(new AutomationRule(
                4,
                Map.of("power", 1.0f),
                "06:00-07:00"
        ));

        return rules;
    }
}