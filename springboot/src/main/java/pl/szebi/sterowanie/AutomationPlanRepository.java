package pl.szebi.sterowanie;

import pl.szebi.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

public class AutomationPlanRepository {
    private List<AutomationPlan> plans = new ArrayList<>();
    private static final ObjectMapper OM = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // lista AutomationRule -> JSON
    private String rulesToJson(List<AutomationRule> rules) {
        try {
            if (rules == null) rules = new ArrayList<>();
            return OM.writeValueAsString(rules);
        } catch (Exception e) {
            throw new RuntimeException("rulesToJson failed", e);
        }
    }

    // JSON -> lista AutomationRule
    private List<AutomationRule> parseRules(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return OM.readValue(json, new TypeReference<List<AutomationRule>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // zapis do bazy danych nowego planu automatyzacji lub modyfikacja istniejącego (to samo id)
    public Boolean save(AutomationPlan plan) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String rulesJson = rulesToJson(plan.getRules());
            PGobject jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(rulesJson);

            if (plan.getId() == null) {
                String sql = "INSERT INTO automation_plan (name, rules) VALUES (?, ?) RETURNING id";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, plan.getName());
                ps.setObject(2, jsonb);
                rs = ps.executeQuery();
                if (rs.next()) {
                    plan.setId(rs.getInt("id"));
                }
                return true;
            } else {
                String sql = "UPDATE automation_plan SET name = ?, rules = ? WHERE id = ?";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, plan.getName());
                ps.setObject(2, jsonb);
                ps.setInt(3, plan.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    public boolean load() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT id, name, rules::text AS rules FROM automation_plan";
            ps = Db.conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("id");
                boolean exists = plans.stream()
                        .anyMatch(p -> id.equals(p.getId()));
                if (exists) {
                    continue;
                }
                String name = rs.getString("name");
                String json = rs.getString("rules");
                List<AutomationRule> rules = parseRules(json);
                AutomationPlan plan = new AutomationPlan(name, rules);
                plan.setId(id);
                plans.add(plan);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    public Boolean add(AutomationPlan plan) {
        if (plan == null) return false;
        return plans.add(plan);
    }

    // usuniecie z bazy i z listy
    public Boolean delete(Integer planId) {
        if (planId == null) return false;
        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM automation_plan WHERE id = ?";
            ps = Db.conn.prepareStatement(sql);
            ps.setInt(1, planId);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                return false;
            }
            synchronized (plans) {
                plans.removeIf(plan -> plan.getId() != null && plan.getId().equals(planId));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    public AutomationPlan findById(Integer planId) {
        for (AutomationPlan p : plans) {
            if (p.getId() != null && p.getId().equals(planId)) {
                return p;
            }
        }
        return null;
    }

    public List<AutomationPlan> findAll() {
        return plans;
    }
}
