package pl.szebi.optimization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import pl.szebi.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OptimizationPlanRepository {
    private static final ObjectMapper OM = new ObjectMapper();

    // Pobiera wszystkie plany
    public List<OptimizationPlan> findAll() {
        List<OptimizationPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM optimization_plan ORDER BY id DESC";

        // Używamy Db.conn, ale NIE w try-with-resources dla Connection!
        try (PreparedStatement stmt = Db.conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OptimizationPlan plan = mapRow(rs);
                if (plan != null) plans.add(plan);
            }
        } catch (SQLException e) {
            System.err.println("Błąd pobierania planów: " + e.getMessage());
            e.printStackTrace();
        }
        return plans;
    }

    public OptimizationPlan findById(Integer id) {
        String sql = "SELECT * FROM optimization_plan WHERE id = ?";
        try (PreparedStatement stmt = Db.conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(OptimizationPlan plan) {
        if (plan.getId() == null) {
            return insert(plan);
        } else {
            return update(plan);
        }
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM optimization_plan WHERE id = ?";
        try (PreparedStatement stmt = Db.conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateName(Integer id, String name) {
        String sql = "UPDATE optimization_plan SET name = ? WHERE id = ?";
        try (PreparedStatement stmt = Db.conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean insert(OptimizationPlan plan) {
        String sql = "INSERT INTO optimization_plan (user_id, status, cost_savings, co2_savings, rules, optimization_strategy, name) " +
                "VALUES (?, ?::plan_status_enum, ?, ?, ?::jsonb, ?::plan_strategy_enum, ?) RETURNING id";

        try (PreparedStatement stmt = Db.conn.prepareStatement(sql)) {
            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getStatus().name());
            stmt.setDouble(3, plan.getCostSavings() != null ? plan.getCostSavings() : 0.0);
            stmt.setDouble(4, plan.getCo2Savings() != null ? plan.getCo2Savings() : 0.0);
            stmt.setString(5, plan.getRulesAsJson());
            stmt.setString(6, plan.getStrategyType().name());
            stmt.setString(7, plan.getName() != null ? plan.getName() : "Plan " + System.currentTimeMillis());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    plan.setId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean update(OptimizationPlan plan) {
        String sql = "UPDATE optimization_plan SET status = ?::plan_status_enum, cost_savings = ?, co2_savings = ?, rules = ?::jsonb, optimization_strategy = ?::plan_strategy_enum WHERE id = ?";
        try (PreparedStatement stmt = Db.conn.prepareStatement(sql)) {
            stmt.setString(1, plan.getStatus().name());
            stmt.setDouble(2, plan.getCostSavings());
            stmt.setDouble(3, plan.getCo2Savings());
            stmt.setString(4, plan.getRulesAsJson());
            stmt.setString(5, plan.getStrategyType().name());
            stmt.setInt(6, plan.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private OptimizationPlan mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        Integer userId = rs.getInt("user_id");
        String strategyStr = rs.getString("optimization_strategy");
        String statusStr = rs.getString("status");
        String rulesJson = rs.getString("rules");
        String name = null;
        try { name = rs.getString("name"); } catch (Exception ignore) {}

        OptimizationStrategy strategy = createStrategy(strategyStr);
        if (strategy == null) return null;

        OptimizationPlan plan = new OptimizationPlan(id, userId, strategy);
        plan.setCostSavings(rs.getDouble("cost_savings"));
        plan.setCo2Savings(rs.getDouble("co2_savings"));
        plan.setName(name);

        try {
            if (rulesJson != null && !rulesJson.isBlank()) {
                List<AutomationRule> rules = OM.readValue(rulesJson, new TypeReference<List<AutomationRule>>() {});
                plan.setRules(rules);
            }
        } catch (Exception e) {
            // Ignorujemy błędy parsowania reguł, ustawiamy pustą listę
            plan.setRules(new ArrayList<>());
        }

        try {
            plan.setStatus(PlanStatus.valueOf(statusStr));
        } catch (Exception e) {
            plan.setStatus(PlanStatus.Draft);
        }

        return plan;
    }

    private OptimizationStrategy createStrategy(String typeName) {
        if (typeName == null) return null;
        try {
            OptimizationStrategyType type = OptimizationStrategyType.valueOf(typeName);
            return switch (type) {
                case Costs_reduction -> new CostReductionStrategy();
                case Load_reduction -> new LoadReductionStrategy();
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}