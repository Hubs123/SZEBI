package com.projekt.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class OptimizationMockData {

    public static OptimizationData generateMockForecastData() {
        OptimizationData data = new OptimizationData();
        Date now = new Date();

        // Symulacja 24-godzinnej prognozy zużycia (większe zużycie rano i wieczorem)
        List<Float> consumed = Arrays.asList(0.5f, 0.4f, 0.4f, 0.3f, 0.5f, 1.0f, 2.5f, 3.0f, 2.8f, 2.5f, 2.0f, 1.8f, 1.5f, 1.6f, 1.7f, 2.2f, 3.5f, 4.0f, 3.8f, 2.5f, 1.5f, 1.0f, 0.8f, 0.6f);
        // Symulacja sprzedaży 
        List<Float> sold = Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.5f, 1.2f, 1.0f, 0.8f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        // Symulacja stanu magazynowania 
        List<Float> stored = Arrays.asList(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f);
        // Symulacja generacji (tylko w słoneczne godziny)
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

    public static List<AutomationRule> generateMockAutomationRules() {
        List<AutomationRule> rules = new ArrayList<>();
        
        return rules;
    }
}