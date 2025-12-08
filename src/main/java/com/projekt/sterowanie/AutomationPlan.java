package com.projekt.sterowanie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutomationPlan {
    private Integer id;
    private String name;
    private List<AutomationRule> rules = new ArrayList<>();

    public AutomationPlan(String name, List<AutomationRule> rules) {
        this.name = name;
        if (rules != null) this.rules.addAll(rules);
    }

    public Integer getId() {
        return id;
    }
    void setId(int id) { this.id = id; }

    public String getName() {
        return name;
    }

    public List<AutomationRule> getRules() {
        return List.copyOf(rules);
    }

    public Boolean addRule(AutomationRule rule) {
        if (rule == null) return false;
        return rules.add(rule);
    }
}
