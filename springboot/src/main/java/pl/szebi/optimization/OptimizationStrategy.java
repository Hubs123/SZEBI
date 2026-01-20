package pl.szebi.optimization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.szebi.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class OptimizationStrategy {

    private static final ObjectMapper OM = new ObjectMapper();

    public abstract boolean calculate(OptimizationPlan plan, OptimizationData data, List<AutomationRule> currentRules);

    // --- Metody pomocnicze ---

    /**
     * Generuje napis "HH:mm-HH:mm" dla danego indeksu okna 4-godzinnego.
     */
    protected String getWindowString(Date simulationStart, int windowIndex) {
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTime(simulationStart);

        // Start okna
        cal.add(Calendar.HOUR_OF_DAY, windowIndex * 4);
        String start = hourFormat.format(cal.getTime());

        // Koniec okna
        cal.add(Calendar.HOUR_OF_DAY, 4);
        String end = hourFormat.format(cal.getTime());

        return start + "-" + end;
    }

    public List<AutomationRule> parseRules(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return OM.readValue(json, new TypeReference<List<AutomationRule>>() {});
        } catch (Exception e) {
            System.err.println("Błąd parsowania JSON reguł: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<AutomationRule> loadRulesFromDatabase() {
        String sql = "SELECT rules FROM automation_plan ORDER BY id DESC LIMIT 1";
        List<AutomationRule> allRules = new ArrayList<>();
        try (PreparedStatement statement = Db.conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                String rulesJson = rs.getString("rules");
                if (rulesJson != null && !rulesJson.isBlank()) {
                    allRules.addAll(parseRules(rulesJson));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allRules;
    }
}