package com.projekt.optimization;

import java.util.*;

// Uwaga: Klasa została zmodyfikowana, aby usunąć bezpośrednią zależność od bazy danych (Db.java)
// i umożliwić wstrzykiwanie/używanie mockowych reguł na potrzeby testów i symulacji.
public abstract class OptimizationStrategy {

    // Abstrakcyjna metoda obliczająca plan optymalizacji
    public abstract boolean calculate(OptimizationPlan plan, OptimizationData data, List<AutomationRule> currentRules);

    // Będzie używane do konwertowania JSON z bazy danych na obiekty
    public List<AutomationRule> parseRules(String json) {

        List<AutomationRule> list = new ArrayList<>();
        json = json.trim();
        if (json.length() <= 2) return list;
        // Uproszczone parsowanie (jak w oryginale)
        String[] ruleBlocks = json.substring(1, json.length() - 1).split("\\},\\{");
        for (String block : ruleBlocks) {
            String clean = block.replace("{", "").replace("}", "");
            String[] fields = clean.split(",");
            int deviceId = -1;
            Map<String, Float> states = new HashMap<>();
            String timeWindow = "placeholder";
            for (String f : fields) {
                String[] kv = f.split(":");
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").trim();
                switch (key) {
                    case "deviceId":
                        deviceId = Integer.parseInt(value);
                        break;
                    case "timeWindow":
                        timeWindow = value;
                        break;
                    default:
                        states.put(key, Float.parseFloat(value));
                }
            }
            AutomationRule rule = new AutomationRule(deviceId, states, timeWindow);
            list.add(rule);
        }
        return list;
    }

    /**
     * Parsuje okno czasowe w formacie "HH:00-HH:00" do pary godzin (Start, Koniec).
     */
    public List<Integer> parseOffTimeWindow(String window) {
        if (window == null || !window.matches("\\d{2}:00-\\d{2}:00")) {
            return Arrays.asList(-1, -1); // Niepoprawny format
        }
        List<Integer> result = new ArrayList<>();
        String[] parts = window.split("-");
        try {
            int startHour = Integer.parseInt(parts[0].split(":")[0]);
            int endHour = Integer.parseInt(parts[1].split(":")[0]);
            result.add(startHour);
            result.add(endHour);
            return result;
        } catch (NumberFormatException e) {
            return Arrays.asList(-1, -1);
        }
    }

    /**
     * Znajduje jedno-godzinne okno z maksymalną wartością w danych (np. maksymalne zużycie/generacja).
     */
    String findMaxWindow(List<Float> data) {
        if (data == null || data.isEmpty()) return "00:00-01:00";

        Float maxConsumption = Collections.max(data);
        int maxIndex = data.indexOf(maxConsumption);

        int peakStartHour = maxIndex;
        // Godzinne okno: od godziny rozpoczęcia do następnej pełnej godziny
        int peakEndHour = (maxIndex + 1) % 24;

        // Jeśli peakEndHour to 0, wyświetlamy 24 dla 23:00-24:00
        return String.format("%02d:00-%02d:00", peakStartHour, peakEndHour == 0 ? 24 : peakEndHour);
    }

    /**
     * Znajduje jedno-godzinne okno z minimalną wartością w danych (np. minimalne zużycie/generacja).
     */
    String findMinWindow(List<Float> data) {
        if (data == null || data.isEmpty()) return "00:00-01:00";

        Float minConsumption = Collections.min(data);
        int minIndex = data.indexOf(minConsumption);

        int offPeakStartHour = minIndex;
        // Godzinne okno: od godziny rozpoczęcia do następnej pełnej godziny
        int offPeakEndHour = (minIndex + 1) % 24;

        // Jeśli offPeakEndHour to 0, wyświetlamy 24 dla 23:00-24:00
        return String.format("%02d:00-%02d:00", offPeakStartHour, offPeakEndHour == 0 ? 24 : offPeakEndHour);
    }
}