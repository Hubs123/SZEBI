package pl.szebi.optimization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.szebi.db.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OptimizationPlanRepository {
    private static final ObjectMapper OM = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public List<OptimizationPlan> findAll(){
        System.out.println("Metoda findAll jeszcze nie łączy się z bazą danych na potrzeby symulacji.");
        return new ArrayList<>();
    }

    public OptimizationPlan findById(Integer id) {
        String sql = "SELECT * FROM optimization_plan WHERE id = ?";
        OptimizationPlan plan = null;

        // UWAGA: Nie wkładaj Db.conn do try(), bo zostanie zamknięte!
        // Wkładamy tam tylko PreparedStatement i ResultSet.
        try (PreparedStatement statement = Db.conn.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // 1. Pobieranie prostych pól
                    Integer planId = rs.getInt("id");
                    Integer userId = rs.getInt("user_id");
                    String statusStr = rs.getString("status");
                    Double costSavings = rs.getDouble("cost_savings");
                    Double co2Savings = rs.getDouble("co2_savings");

                    // 2. Parsowanie reguł (Obsługa błędu Jacksona)
                    String rulesJson = rs.getString("rules");
                    List<AutomationRule> rules = parseRules(rulesJson);

                    // 3. Tworzenie strategii (Zgodnie z Diagramem Enumów)
                    String strategyName = rs.getString("optimization_strategy");
                    OptimizationStrategy strategy = createStrategyFactory(strategyName);

                    if (strategy == null) {
                        System.err.println("Nieznana strategia w bazie: " + strategyName);
                        return null;
                    }

                    // 4. Budowanie obiektu
                    plan = new OptimizationPlan(planId, userId, strategy);
                    plan.setRules(rules);
                    plan.setCo2Savings(co2Savings); // Twoje settery mogą przyjmować BigDecimal, sprawdź to
                    plan.setCostSavings(costSavings);

                    // Bezpieczna konwersja statusu
                    try {
                        plan.setStatus(PlanStatus.valueOf(statusStr));
                    } catch (IllegalArgumentException e) {
                        plan.setStatus(PlanStatus.DRAFT); // Wartość domyślna w razie błędu
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plan;
    }

    public boolean save(OptimizationPlan plan) {
        // Rozpoznanie operacji: Brak ID -> Nowy (INSERT), Jest ID -> Aktualizacja (UPDATE)
        if (plan.getId() == null) {
            return insertPlan(plan);
        } else {
            return updatePlan(plan);
        }
    }

    private boolean insertPlan(OptimizationPlan plan) {
        String sql = "INSERT INTO optimization_plan (user_id, status, cost_savings, co2_savings, rules, optimization_strategy) " +
                "VALUES (?, ?::plan_status_enum, ?, ?, ?::jsonb, ?::plan_strategy_enum) RETURNING id";

        // UWAGA: Nie wkładaj 'Db.conn' do try(), bo to zamknie połączenie dla całej aplikacji!
        try (PreparedStatement statement = Db.conn.prepareStatement(sql)) {

            // 1. Ustawienie parametrów
            statement.setInt(1, plan.getUserId());
            statement.setString(2, plan.getStatus().name()); // Np. "ACTIVE"
            statement.setDouble(3, plan.getCostSavings());
            statement.setDouble(4, plan.getCo2Savings());
            statement.setString(5, plan.getRulesAsJson());
            statement.setString(6, plan.getStrategyName()); // Np. "CostReduction"

            // 2. Wykonanie i pobranie ID
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    plan.setId(generatedId);
                    System.out.printf("   [DB] Zapisano nowy Plan ID: %d.%n", generatedId);
                    return true;
                }
            }
        } catch (Exception e) {
            handleDbError(e, plan, "INSERT");
        }
        return false;
    }

    private boolean updatePlan(OptimizationPlan plan) {
        String sql = "UPDATE optimization_plan SET " +
                "status = ?::plan_status_enum, " +
                "cost_savings = ?, " +
                "co2_savings = ?, " +
                "rules = ?::jsonb, " +
                "optimization_strategy = ?::plan_strategy_enum " +
                "WHERE id = ?";

        try (PreparedStatement statement = Db.conn.prepareStatement(sql)) {

            // 1. Ustawienie parametrów do aktualizacji
            statement.setString(1, plan.getStatus().name());
            statement.setDouble(2, plan.getCostSavings());
            statement.setDouble(3, plan.getCo2Savings());
            statement.setString(4, plan.getRulesAsJson());
            statement.setString(5, plan.getStrategyName());

            // Warunek WHERE id = ?
            statement.setInt(6, plan.getId());

            // 2. Wykonanie
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("   [DB] Zaktualizowano Plan ID: %d.%n", plan.getId());
                return true;
            } else {
                System.err.println("   [DB] Nie znaleziono planu o ID: " + plan.getId() + " do aktualizacji.");
            }

        } catch (Exception e) {
            handleDbError(e, plan, "UPDATE");
        }
        return false;
    }

    // Metoda pomocnicza do obsługi błędów (unika powielania kodu)
    private void handleDbError(Exception e, OptimizationPlan plan, String operation) {
        System.err.println("Błąd operacji " + operation + " na bazie danych: " + e.getMessage());
        e.printStackTrace();
        // Zabezpieczenie: jeśli zapis się nie udał, oznaczamy obiekt jako STOPPED
        // (choć w bazie może nadal być stary status, to w pamięci aplikacji wiemy o błędzie)
        try {
            plan.setStatus(PlanStatus.Stopped);
        } catch (Exception ex) {
            // Ignorujemy błędy przy ustawianiu statusu w razie totalnej awarii
        }
    }

    // Metoda obsługująca endpoint zmiany nazwy
    public boolean updateName(Integer id, String name) {
        String sql = "UPDATE optimization_plan SET name = ? WHERE id = ?";
        // Wykonaj update w DB...
        return true;
    }

    // lista AutomationRule -> JSON
//    private String rulesToJson(List<AutomationRule> rules) {
//        try {
//            if (rules == null) rules = new ArrayList<>();
//            return OM.writeValueAsString(rules);
//        } catch (Exception e) {
//            throw new RuntimeException("rulesToJson failed", e);
//        }
//    }

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

    private OptimizationStrategy createStrategyFactory(String typeName) {
        if (typeName == null) return null;

        // Normalizacja nazwy (opcjonalnie, jeśli w bazie masz bałagan)
        // Np. zamiana "Costs_reduction" na "CostReduction"

        try {
            OptimizationStrategyType type = OptimizationStrategyType.valueOf(typeName);

            return switch (type) {
                case Costs_reduction -> new CostReductionStrategy();
                case Co2_reduction -> new Co2ReductionStrategy();
                case Load_reduction -> new LoadReductionStrategy();
                // default -> null; // Enum pokrywa wszystko
            };
        } catch (IllegalArgumentException e) {
            // Fallback dla starych nazw z bazy, jeśli nie pasują do Enuma
            if (typeName.equalsIgnoreCase("Costs_reduction")) return new CostReductionStrategy();
            if (typeName.equalsIgnoreCase("Co2_reduction")) return new Co2ReductionStrategy();
            return null;
        }
    }
}