package pl.szebi.optimization.api;

import org.springframework.web.bind.annotation.*;
import pl.szebi.optimization.*;

import java.util.List;

@RestController
@RequestMapping("/api/optimization")
@CrossOrigin(origins = "*") // Allow frontend access
public class OptimizationController {

    private final OptimizationManager optimizationManager;
    private final OptimizationPlanRepository planRepo;

    public OptimizationController(OptimizationManager optimizationManager) {
        this.optimizationManager = optimizationManager;
        this.planRepo = new OptimizationPlanRepository();
    }

    @GetMapping("/plans")
    public List<OptimizationPlan> getPlans() {
        return planRepo.findAll();
    }

    @PostMapping("/generate")
    public OptimizationPlan generatePlan(@RequestParam Integer userId, @RequestParam String strategyType) {
        return optimizationManager.generatePlan(userId, strategyType);
    }

    @PostMapping("/plans/{id}/run")
    public boolean runPlan(@PathVariable Integer id) {
        return optimizationManager.runPlan(id);
    }

    @PostMapping("/plans/{id}/stop")
    public boolean stopPlan(@PathVariable Integer id) {
        return optimizationManager.stopPlan(id);
    }

    @DeleteMapping("/plans/{id}")
    public boolean deletePlan(@PathVariable Integer id) {
        return optimizationManager.deletePlan(id);
    }

    @PatchMapping("/plans/{id}/rename")
    public boolean renamePlan(@PathVariable Integer id, @RequestBody String newName) {
        String cleaned = newName.replace("\"", "");
        return planRepo.updateName(id, cleaned);
    }
}