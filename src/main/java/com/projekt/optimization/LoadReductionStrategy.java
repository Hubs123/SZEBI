package com.projekt.optimization;

import com.projekt.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

        List<AutomationRule> rules = getRulesFromDatabase();
        
        //generowanie regul dla urządzeń
        for (AutomationRule rule : rules) {
            //wyłączenie w godzinach szczytu
            Map<String, Float> states = rule.getStates();
            if (states.get("power") == null) {
                return false;
            }
            if (states.get("power") == 0.0) {
                continue;
            }
            Map<String, Float> offState = new HashMap<>();
            offState.put("power", 0.0f); 
            AutomationRule ruleOff = new AutomationRule(null, offState, peakLoadWindow);
            calculatedRules.add(ruleOff);
            
            //włączenie w godzinach niskiego obciążenia
            Map<String, Float> onState = new HashMap<>();
            onState.put("power", 1.0f); 
            AutomationRule ruleOn = new AutomationRule(null, onState, offPeakLoadWindow);
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

    private List<AutomationRule> parseRules(String json) {
        List<AutomationRule> list = new ArrayList<>();
        json = json.trim();
        if (json.length() <= 2) return list;  // pusta lista ("[]")
        String[] ruleBlocks = json.substring(1, json.length() - 1).split("\\},\\{");
        for (String block : ruleBlocks) {
            String clean = block.replace("{", "").replace("}", "");
            String[] fields = clean.split(",");
            // przy błędnym odczycie (-1 nie nadpisane) rule nie zostanie potem zastosowany dla żadnego urządzenia
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

    public List<AutomationRule> getRulesFromDatabase() {
        List<AutomationRule> rulesList = new ArrayList<>();
        String sql = "SELECT rules::text AS rules_text FROM automation_plan";

        try (PreparedStatement statement = Db.getConnection().prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String rulesJson = rs.getString("rules_text");
                rulesList = parseRules(rulesJson);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rulesList;
    }

}
