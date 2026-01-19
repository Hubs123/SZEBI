package pl.szebi.optimization;

import pl.szebi.db.Db; // Import do połączenia z bazą danych
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OptimizationPlanRepository {

    public List<OptimizationPlan> findAll() {
        List<OptimizationPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM optimization_plan ORDER BY id DESC";

        try (Connection conn = Db.conn;
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                plans.add(mapResultSetToPlan(rs)); // Korzystamy z logiki mapowania
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    public boolean save(OptimizationPlan plan) {
        // Twoja metoda save z rzutowaniem na typy PostgreSQL jest poprawna
        String sql = "INSERT INTO optimization_plan (user_id, status, cost_savings, co2_savings, rules, optimization_strategy, name) " +
                "VALUES (?, ?::plan_status_enum, ?, ?, ?::jsonb, ?::plan_strategy_enum, ?) RETURNING id";

        try (Connection conn = Db.conn;
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, plan.getUserId());
            statement.setString(2, plan.getStatus().name());
            statement.setDouble(3, plan.getCostSavings());
            statement.setDouble(4, plan.getCo2Savings());
            statement.setString(5, plan.getRulesAsJson());
            statement.setString(6, plan.getStrategyName());
            statement.setString(7, plan.getName()); // Dodano zapis nazwy

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    plan.setId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public OptimizationPlan findById(Integer id) {
        // Implementacja findById jest poprawna i naprawia błąd w OptimizationManager
        String sql = "SELECT * FROM optimization_plan WHERE id = ?";
        try (Connection conn = Db.conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlan(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateName(Integer id, String name) {
        String sql = "UPDATE optimization_plan SET name = ? WHERE id = ?";
        try (Connection conn = Db.conn;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Wydzielona metoda mapowania - unika powielania kodu w findAll i findById
    private OptimizationPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        String strategyType = rs.getString("optimization_strategy");
        OptimizationStrategy strategy = switch (strategyType) {
            case "Costs_reduction" -> new CostReductionStrategy();
            case "Co2_reduction" -> new Co2ReductionStrategy();
            default -> new LoadReductionStrategy();
        };

        OptimizationPlan plan = new OptimizationPlan(rs.getInt("user_id"), strategy);
        plan.setId(rs.getInt("id"));
        plan.setName(rs.getString("name"));
        plan.setStatus(PlanStatus.valueOf(rs.getString("status")));
        plan.setCostSavings(rs.getDouble("cost_savings"));
        plan.setCo2Savings(rs.getDouble("co2_savings"));
        plan.setRules(strategy.parseRules(rs.getString("rules")));
        return plan;
    }
}