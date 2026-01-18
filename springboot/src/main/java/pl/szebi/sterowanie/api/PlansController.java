package pl.szebi.sterowanie.api;

import pl.szebi.sterowanie.AutomationPlan;
import pl.szebi.sterowanie.AutomationPlanManager;
import pl.szebi.sterowanie.AutomationRule;

import pl.szebi.sterowanie.api.ControlDtos.CreatePlanRequest;
import pl.szebi.sterowanie.api.ControlDtos.AddRuleRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control/plans")
// @CrossOrigin
public class PlansController {

    private final AutomationPlanManager planManager;

    public PlansController() {
        planManager = new AutomationPlanManager();
        planManager.loadFromDatabase();
    }

    @GetMapping
    public ResponseEntity<?> listPlans() {
        return ResponseEntity.ok(planManager.listPlans());
    }

    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlan(@PathVariable Integer planId) {
        AutomationPlan p = planManager.getPlan(planId);
        if (p == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono planu.");
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody CreatePlanRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return ResponseEntity.badRequest().body("Niepoprawna nazwa planu.");
        }
        if (req.rules == null || req.rules.isEmpty()) {
            return ResponseEntity.badRequest().body("Plan musi zawierać przynajmniej jedną regułę.");
        }
        AutomationPlan plan = planManager.createPlan(req.name, req.rules);
        if (plan == null) return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się utworzyć planu.");
        boolean res = planManager.saveToDatabase(plan);
        if (!res) return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się zapisać planu do bazy danych.");
        return ResponseEntity.status(HttpStatus.CREATED).body(plan.getId());
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Integer planId) {
        Boolean ok = planManager.removePlan(planId);
        if (ok == null || !ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono planu.");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{planId}/activate")
    public ResponseEntity<?> activatePlan(@PathVariable Integer planId) {
        Boolean ok = planManager.activatePlan(planId);
        return ok ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się aktywować planu.");
    }

    @PostMapping("/{planId}/rules")
    public ResponseEntity<?> addRule(@PathVariable Integer planId, @RequestBody AddRuleRequest req) {
        if (req == null || req.deviceId == null || req.states == null || req.states.isEmpty()) {
            return ResponseEntity.badRequest().body("Niepoprawna reguła.");
        }
        AutomationRule rule = new AutomationRule(req.deviceId, req.states, req.timeWindow);
        boolean ok = planManager.addRule(planId, rule);
        return ok ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono planu.");
    }
}
