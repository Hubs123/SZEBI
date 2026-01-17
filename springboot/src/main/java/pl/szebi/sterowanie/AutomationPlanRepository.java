package pl.szebi.sterowanie;

import pl.szebi.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomationPlanRepository {
    private List<AutomationPlan> plans = new ArrayList<>();

    // lista AutomationRule's -> json do bazy
    private String rulesToJson(List<AutomationRule> rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < rules.size(); i++) {
            AutomationRule r = rules.get(i);
            sb.append("{");
            sb.append("\"deviceId\":").append(r.getDeviceId()).append(",");
            sb.append("\"timeWindow\":\"").append(r.getTimeWindow()).append("\",");
            sb.append("\"states\":{");
            int cnt = 0;
            for (Map.Entry<String, Float> e : r.getStates().entrySet()) {
                sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
                if (++cnt < r.getStates().size()) sb.append(",");
            }
            sb.append("}");
            sb.append("}");
            if (i < rules.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // json z bazy -> lista AutomationRule's
    // helper potencjalnie do przekazania modułowi akwizycji danych
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

    // zapis do bazy danych nowego planu automatyzacji lub modyfikacja istniejącego (to samo id)
    public Boolean save(AutomationPlan plan) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String rulesJson = rulesToJson(plan.getRules());
            if (plan.getId() == null) {
                String sql = "INSERT INTO automation_plan (name, rules) VALUES (?, ?::jsonb) RETURNING id";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, plan.getName());
                ps.setString(2, rulesJson);
                rs = ps.executeQuery();
                if (rs.next()) {
                    plan.setId(rs.getInt("id"));
                }
                return true;
            } else {
                String sql = "UPDATE automation_plan SET name = ?, rules = ?::jsonb WHERE id = ?";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, plan.getName());
                ps.setString(2, rulesJson);
                ps.setInt(3, plan.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // zamknięcie statement i result set, bez obsługi wyjątków (nie ma za bardzo co zrobić)
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    // metoda przykładowa, na razie nie robimy więcej metod tego typu - nie było tego w planie
    // i prawdopodobnie będzie robił to moduł akwizycji danych
    public AutomationPlan getFromDatabase(int id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT id, name, rules::text FROM automation_plan WHERE id = ?";
            ps = Db.conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            String name = rs.getString("name");
            String json = rs.getString("rules");
            List<AutomationRule> rules = parseRules(json);
            return new AutomationPlan(name, rules);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }

    public Boolean add(AutomationPlan plan) {
        if (plan == null) return false;
        if (plan.getId() == null) return false;
        return plans.add(plan);
    }

    public Boolean delete(Integer planId) {
        if (plans.isEmpty()) return false;
        return plans.removeIf(plan -> plan.getId() != null && plan.getId().equals(planId));
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
