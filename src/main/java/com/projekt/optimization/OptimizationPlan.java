package com.projekt.optimization;

import java.util.List;
import java.util.Map;

public class OptimizationPlan {
    private Integer id;
    private final Integer userId;
    private PlanStatus status; // Używamy importowanego enuma
    private Double costSavings = 0.0;
    private Double co2Savings = 0.0;
    private final OptimizationStrategy strategy;
    private final OptimizationStrategyType strategyType;
    private List<AutomationRule> rules;

    public OptimizationPlan(Integer userId, OptimizationStrategy strategy) {
        this.userId = userId;
        this.strategy = strategy;

        String strategyClassName = strategy.getClass().getSimpleName().replace("Strategy", "");
        strategy.getClass().getSimpleName().replace("Strategy", "");
        if (strategyClassName.equals("LoadReduction")) {
            strategyClassName = "Load_reduction";
        }
        if (strategyClassName.equals("Co2Reduction")) {
            strategyClassName = "Co2_reduction";
        }
        if (strategyClassName.equals("CostReduction")) {
            strategyClassName = "Costs_reduction";
        }
        this.strategyType = OptimizationStrategyType.valueOf(strategyClassName);

        this.status = PlanStatus.Draft;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Double getCostSavings() {
        return costSavings;
    }

    public Double getCo2Savings() {
        return co2Savings;
    }

    public OptimizationStrategy getStrategy() {
        return strategy;
    }

    public String getStrategyName() {
        return strategyType.name();
    }

    public OptimizationStrategyType getStrategyType() {
        return strategyType;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public List<AutomationRule> getRules() {
        return rules;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public void setCostSavings(Double costSavings) {
        this.costSavings = costSavings;
    }

    public void setCo2Savings(Double co2Savings) {
        this.co2Savings = co2Savings;
    }

    public void setRules(List<AutomationRule> rules) {
        this.rules = rules;
        if (rules != null && this.status == PlanStatus.Draft) {
            this.status = PlanStatus.Pending;
        }
    }

    // lista AutomationRule's -> json do bazy
    public String getRulesAsJson() {
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

    @Override
    public String toString() {
        return "OptimizationPlan{" +
                "id=" + id +
                ", userId=" + userId +
                ", status=" + status +
                ", costSavings=" + costSavings +
                ", co2Savings=" + co2Savings +
                ", strategy=" + strategyType.name() +
                ", rulesSize=" + (rules != null ? rules.size() : 0) +
                '}';
    }
}