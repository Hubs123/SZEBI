import java.util.List;

public class OptimizationPlan {
    private Integer id;
    private Integer userId;
    //private PlanStatus status;
    private Double costSavings = 0.0;
    private Double co2Savings = 0.0;
    private OptimizationStrategy strategy;
    private List<AutomationRule> rules;

    //Na razie zostawiam enum, bo jest zależny od logiki działania
    public OptimizationPlan(Integer userId, OptimizationStrategy strategy) {
        this.userId = userId;
        this.strategy = strategy;
        //this.status = PlanStatus.DRAFT;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int size) {
        this.id = size;
    }
}
