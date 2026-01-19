package pl.szebi.optimization.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.szebi.optimization.OptimizationManager;
import pl.szebi.optimization.OptimizationPlan;
import pl.szebi.optimization.OptimizationPlanRepository;

import java.util.List;

@RestController
@RequestMapping("api/optimization")
public class OptimizationController {

    private OptimizationManager optimizationManager;

//    OptimizationController() {
//
//    }

//    @GetMapping("/plans")
//    public List<OptimizationPlan> getPlans() {
//        return
//    }
}
