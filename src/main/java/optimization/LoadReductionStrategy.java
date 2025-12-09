package optimization;

public class LoadReductionStrategy extends OptimizationStrategy {

    //lista urządzeń, które możemy przesuwać
    private static final List<Integer> SHIFTABLE_DEVICES = Arrays.asList(301, 302);

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
        Double peakLoadReductionKw = 0.0;
        
        //generowanie regul dla urządzeń 
        for (Integer deviceId : SHIFTABLE_DEVICES) {
            
            //wyłączenie w godzinach szczytu
            Map<String, Float> offState = new HashMap<>();
            offState.put("power", 0.0f); 
            AutomationRule ruleOff = new AutomationRule(deviceId, offState, peakLoadWindow);
            calculatedRules.add(ruleOff);
            
            //włączenie w godzinach niskiego obciążenia
            Map<String, Float> onState = new HashMap<>();
            onState.put("power", 1.0f); 
            AutomationRule ruleOn = new AutomationRule(deviceId, onState, offPeakLoadWindow);
            calculatedRules.add(ruleOn);
            
            peakLoadReductionKw += 2.0; 
        }
        
        //zapisanie wyniku
        plan.setRules(calculatedRules);
        plan.setCo2Savings(peakLoadReductionKw);
        
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
