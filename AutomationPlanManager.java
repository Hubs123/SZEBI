import java.util.ArrayList;
import java.util.List;

public class AutomationPlanManager {
    private AutomationPlanRepository planRepo;
    private List<String> protectedStates = new ArrayList<>();

    public AutomationPlanManager(AutomationPlanRepository planRepo, List<String> protectedStates) {
        this.planRepo = planRepo;
        this.protectedStates = protectedStates;
    }

    public List<String> getProtectedStates() {
        return new ArrayList<>(protectedStates);
    }

    public Integer createPlan(Integer id, String name, List<AutomationRule> rules) {
        if (rules != null) return null;
        AutomationPlan plan = new AutomationPlan(id, name, rules);
        boolean added = planRepo.add(plan);
        return added ? plan.getId() : null;
    }

    public Boolean removePlan(Integer planId) {
        return planRepo.delete(planId);
    }

    public Boolean activatePlan(Integer planId) {
        //TODO
        return null;
    }

    public Boolean applyModifications(Integer planId, List<AutomationRule> newRules, Integer priority) {
        //TODO
        return null;
    }
}
