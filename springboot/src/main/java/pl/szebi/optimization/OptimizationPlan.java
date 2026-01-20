package pl.szebi.optimization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class OptimizationPlan {
    private Integer id;
    private String name;
    private Integer userId;
    private PlanStatus status;
    private Double costSavings = 0.0;
    private Double co2Savings = 0.0;

    // Ignorujemy obiekt strategii przy wysyłaniu do JSON (zapobiega błędowi pustego obiektu)
    @JsonIgnore
    private OptimizationStrategy strategy;

    private OptimizationStrategyType strategyType;
    private List<AutomationRule> rules = new ArrayList<>();

    public OptimizationPlan() {
    }

    public OptimizationPlan(Integer id, Integer userId, OptimizationStrategy strategy) {
        this.id = id;
        this.userId = userId;
        setStrategy(strategy);
        this.status = PlanStatus.Draft;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public Double getCostSavings() {
        return costSavings;
    }

    public void setCostSavings(Double costSavings) {
        this.costSavings = costSavings;
    }

    public Double getCo2Savings() {
        return co2Savings;
    }

    public void setCo2Savings(Double co2Savings) {
        this.co2Savings = co2Savings;
    }

    public OptimizationStrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(OptimizationStrategyType strategyType) {
        this.strategyType = strategyType;
    }

    public OptimizationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(OptimizationStrategy strategy) {
        this.strategy = strategy;
        if (strategy != null) {
            // Automatyczne ustawienie typu na podstawie klasy
            String cleanName = strategy.getClass().getSimpleName().replace("Strategy", "");
            if (cleanName.equalsIgnoreCase("CostReduction") || cleanName.contains("Cost"))
                this.strategyType = OptimizationStrategyType.Costs_reduction;
            else if (cleanName.equalsIgnoreCase("LoadReduction") || cleanName.contains("Load"))
                this.strategyType = OptimizationStrategyType.Load_reduction;
        }
    }

    public List<AutomationRule> getRules() {
        if (rules == null) return new ArrayList<>();
        return rules;
    }

    public void setRules(List<AutomationRule> rules) {
        this.rules = rules;
    }

    // Metoda pomocnicza dla bazy danych - ukryta przed JSON frontendu
    @JsonIgnore
    public String getRulesAsJson() {
        if (rules == null || rules.isEmpty()) {
            return "[]";
        }
        try {
            return new ObjectMapper().writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @Override
    public String toString() {
        return "OptimizationPlan{" +
                "id=" + id +
                ", status=" + status +
                ", strategy=" + strategyType +
                '}';
    }
}