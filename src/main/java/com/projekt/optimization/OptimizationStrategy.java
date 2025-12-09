package com.projekt.optimization;

import com.projekt.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class OptimizationStrategy {
    public abstract boolean calculate(OptimizationPlan plan, OptimizationData data);

    List<AutomationRule> parseRules(String json) {
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

    List<AutomationRule> getRulesFromDatabase() {
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

    List<Integer> parseOffTimeWindow(String window) {
        // window ma format "HH:00-HH:00"
        List<Integer> result = new ArrayList<>();
        String[] parts = window.split("-");
        int startHour = Integer.parseInt(parts[0].split(":")[0]);
        int endHour = Integer.parseInt(parts[1].split(":")[0]);
        result.add(startHour);
        result.add(endHour);
        return result;
    }

    String findMaxWindow(List<Float> data) {
        Float maxConsumption = Collections.max(data);
        int maxIndex = data.indexOf(maxConsumption);

        int peakStartHour = maxIndex;
        //szukamy godzinnego okna
        int peakEndHour = (maxIndex + 1) % 24;

        return String.format("%02d:00-%02d:00", peakStartHour, peakEndHour);
    }

    String findMinWindow(List<Float> data) {
        Float minConsumption = Collections.min(data);
        int minIndex = data.indexOf(minConsumption);

        int offPeakStartHour = minIndex;
        //szukamy godzinnego okna
        int offPeakEndHour = (minIndex + 1) % 24;

        return String.format("%02d:00-%02d:00", offPeakStartHour, offPeakEndHour);
    }
}
