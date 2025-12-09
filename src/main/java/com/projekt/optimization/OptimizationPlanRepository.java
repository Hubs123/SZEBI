package com.projekt.optimization;

import com.projekt.db.Db; // Import do połączenia z bazą danych
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OptimizationPlanRepository {

    public List<OptimizationPlan> findAll(){
        System.out.println("Uwaga: Metoda findAll nie łączy się z bazą danych na potrzeby symulacji.");
        return new ArrayList<>(); // Stub na potrzeby kompilacji
    }

    public boolean save(OptimizationPlan plan) {
        String sql = "INSERT INTO optimization_plan (user_id, status, cost_savings, co2_savings, rules, optimization_strategy) " +
                "VALUES (?, ?::plan_status_enum, ?, ?, ?::jsonb, ?::plan_strategy_enum) RETURNING id";

        try (Connection conn = Db.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            // 1. Ustawienie parametrów
            statement.setInt(1, plan.getUserId());
            statement.setString(2, plan.getStatus().name());
            statement.setDouble(3, plan.getCostSavings());
            statement.setDouble(4, plan.getCo2Savings());
            statement.setString(5, plan.getRulesAsJson());
            statement.setString(6, plan.getStrategyName());

            // 2. Wykonanie i pobranie wygenerowanego ID
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    plan.setId(rs.getInt(1));
                    System.out.printf("   [DB] Zapisano Plan ID: %d do bazy danych.%n", plan.getId());
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Błąd zapisu planu optymalizacji do bazy danych:");
            e.printStackTrace();
            plan.setStatus(PlanStatus.Stopped);
            return false;
        } catch (IllegalStateException e) {
            System.err.println("Błąd połączenia z bazą: " + e.getMessage());
            plan.setStatus(PlanStatus.Stopped);
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd: " + e.getMessage());
            plan.setStatus(PlanStatus.Stopped);
            return false;
        }
        return false;
    }
}