package com.projekt.optimization;

import java.util.*;

public class LoadReductionStrategy extends OptimizationStrategy {

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data) {
        if (data == null || data.getForecastConsumed() == null || data.getForecastConsumed().size() < 24) {
            return false;
        }
        
        List<Float> consumed = data.getForecastConsumed();

        //okna czasowe dotyczace przedzialow zuzycia
        String peakLoadWindow = findPeakLoadWindow(consumed);
        String offPeakLoadWindow = findOffPeakLoadWindow(consumed);
        
        if (peakLoadWindow == null || offPeakLoadWindow == null) {
             return false;
        }
        
        List<AutomationRule> calculatedRules = new ArrayList<>();

        List<AutomationRule> rules = getRulesFromDatabase();
        
        //generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            //wyłączenie w godzinach szczytu
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
                List<Integer> timeWindowNew = parseOffTimeWindow(offPeakLoadWindow);

                // stary przedział czasowy jest w najlepszym wyliczonym przedziale czasowym
                // nie musimy optymalizowac
                if (timeWindowNew.get(0) <= timeWindowOld.get(0) || timeWindowOld.get(1) <= timeWindowNew.get(1)) {
                    continue;
                }
                rule.setTimeWindow(offPeakLoadWindow);
                calculatedRules.add(rule);
            }
        }
        
        //zapisanie wyniku
        plan.setRules(calculatedRules);
        return true; 
    }

    private String findPeakLoadWindow(List<Float> consumed) {
        Float maxConsumption = Collections.max(consumed);
        int maxIndex = consumed.indexOf(maxConsumption);
        
        int peakStartHour = maxIndex;
        //godzinę max + dwie kolejne
        int peakEndHour = (maxIndex + 2) % 24; 
        
        return String.format("%02d:00-%02d:00", peakStartHour, peakEndHour);
    }
    
    private String findOffPeakLoadWindow(List<Float> consumed) {
        Float minConsumption = Collections.min(consumed);
        int minIndex = consumed.indexOf(minConsumption);
        
        int offPeakStartHour = minIndex;
        //godzinę min + pięć kolejnych
        int offPeakEndHour = (minIndex + 5) % 24; 
        
        return String.format("%02d:00-%02d:00", offPeakStartHour, offPeakEndHour);
    }

}
