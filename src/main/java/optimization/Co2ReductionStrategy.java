package optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Co2ReductionStrategy extends OptimizationStrategy {

    //pompa ciepła
    private static final int HEAT_DEVICE_ID = 401; 
    //temperatura krytyczna
    private static final float CRITICAL_TEMP_THRESHOLD = 20.0f; 
    //okno czasowe wysokiej emisyjności (np. typowa noc, brak słońca)
    private static final String HIGH_CO2_WINDOW = "22:00-06:00";

    @Override
    public boolean calculate(OptimizationPlan plan, OptimizationData data) {
        if (data == null || data.getForecastGenerated() == null || data.getForecastTemperature() == null || data.getForecastGenerated().size() < 24) {
            return false;
        }
        
        List<Float> generated = data.getForecastGenerated();
        List<Float> temperature = data.getForecastTemperature();

        List<AutomationRule> calculatedRules = new ArrayList<>();
        Double co2SavingsKg = 0.0;

        //sprawdzenie warunku krytycznej temperatury
        if (isCriticalTemperaturePredicted(temperature)) {
            //jeśli temperatura spadnie poniżej progu, priorytetem jest natychmiastowe włączenie.
            //ustalenie reguły awaryjnego ogrzewania na całe okno spadku.
            int criticalHour = findFirstCriticalHour(temperature);
            
            Map<String, Float> emergencyState = new HashMap<>();
            emergencyState.put("mode", 1.0f); //włącz ogrzewanie
            
            //reguła awaryjnego włączenia
            AutomationRule emergencyRule = new AutomationRule(HEAT_DEVICE_ID, emergencyState, String.format("%02d:00-23:00", criticalHour));
            
            calculatedRules.add(emergencyRule);
            co2SavingsKg = 0.0; //w przypadku awarii, oszczędności CO2 są zerowane lub ujemne
            
        } else {
            //normalna optymalizacja CO2 (gdy nie ma krytycznej temperatury)
            
            //znajdź okno najniższej emisji (maksymalnej autokonsumpcji PV)
            String lowCo2Window = findLowCo2Window(generated); 

            //wyłączenie w godzinach wysokiej emisji CO2
            Map<String, Float> offState = new HashMap<>();
            offState.put("mode", 0.0f); // Tryb OFF
            AutomationRule ruleOff = new AutomationRule(HEAT_DEVICE_ID, offState, HIGH_CO2_WINDOW);
            calculatedRules.add(ruleOff);

            //włączenie w godzinach niskiej emisji (słoneczne południe)
            Map<String, Float> onState = new HashMap<>();
            onState.put("mode", 1.0f); // Tryb HEAT
            AutomationRule ruleOn = new AutomationRule(HEAT_DEVICE_ID, onState, lowCo2Window);
            calculatedRules.add(ruleOn);
            
            co2SavingsKg = 5.0;
        }
        
        //zapisanie wyniku
        plan.setRules(calculatedRules);
        plan.setCo2Savings(co2SavingsKg); 

        return true; 
    }

    //znajduje okno czasowe najniższej emisji CO2 (maksymalnej generacji)
    private String findLowCo2Window(List<Float> generated) {
        Float maxGeneration = Collections.max(generated);
        int maxIndex = generated.indexOf(maxGeneration);
        
        int lowCo2StartHour = maxIndex;
        int lowCo2EndHour = (maxIndex + 3) % 24; 
        
        return String.format("%02d:00-%02d:00", lowCo2StartHour, lowCo2EndHour);
    }
    
    //sprawdza, czy przewidywana jest krytyczna temperatura
    private boolean isCriticalTemperaturePredicted(List<Float> temperature) {
        for (Float temp : temperature) {
            if (temp < CRITICAL_TEMP_THRESHOLD) {
                return true;
            }
        }
        return false;
    }
    
    //temp ponizej krytycznej, zwraca pierwszą godzinę
    private int findFirstCriticalHour(List<Float> temperature) {
        for (int h = 0; h < temperature.size(); h++) {
            if (temperature.get(h) < CRITICAL_TEMP_THRESHOLD) {
                return h;
            }
        }
        return -1; 
    }
}
