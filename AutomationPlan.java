import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutomationPlan {
    private Integer id;
    private String name;
    private List<AutomationRule> rules = new ArrayList<>();

    public AutomationPlan(Integer id, String name, List<AutomationRule> rules) {
        this.id = id;
        this.name = name;
        if (rules != null) this.rules.addAll(rules);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<AutomationRule> getRules() {
        return Collections.unmodifiableList(new ArrayList<>(rules));
    }

    public Boolean addRule(AutomationRule rule) {
        if (rule == null) return false;
        return rules.add(rule);
    }

}
