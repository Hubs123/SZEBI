package com.projekt.sterowanie;

import java.util.ArrayList;
import java.util.List;

public class AutomationPlanRepository {
    private List<AutomationPlan> plans = new ArrayList<>();

    public Boolean save(AutomationPlan plan) {
        //TODO: zapis do bazy
        return null;
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
        return new ArrayList<>(plans);
    }
}
