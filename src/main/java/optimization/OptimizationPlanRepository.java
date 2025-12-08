package optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OptimizationPlanRepository {
    private List<OptimizationPlan> plans = new ArrayList<>();

    public OptimizationPlan findById(Integer planId) throws ClassNotFoundException {
        for (OptimizationPlan plan : plans) {
            if (Objects.equals(plan.getId(), planId)) {
                return plan;
            }
        }
        throw new ClassNotFoundException("Plan not found");
    }

    public OptimizationPlan findByUser(Integer planId) throws ClassNotFoundException {
        for (OptimizationPlan plan : plans) {
            if (Objects.equals(plan.getId(), planId)) {
                return plan;
            }
        }
        throw new ClassNotFoundException("Plan not found");
    }

    public List<OptimizationPlan> findAll(){
        return plans;
    }


    public boolean delete(Integer planId) {
        try{
            plans.remove(findById(planId));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save(OptimizationPlan plan) {
        try{
            // Wstępnie id przydzielane jako rozmiar listy
            plan.setId(plans.size());
            plans.add(plan);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
