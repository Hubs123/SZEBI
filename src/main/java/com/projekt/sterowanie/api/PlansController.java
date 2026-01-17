package com.projekt.sterowanie.api;

import com.projekt.sterowanie.AutomationPlan;
import com.projekt.sterowanie.AutomationPlanManager;
import com.projekt.sterowanie.AutomationRule;

import com.projekt.sterowanie.api.ControlDtos.CreatePlanRequest;
import com.projekt.sterowanie.api.ControlDtos.AddRuleRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/control/plans")
@CrossOrigin
public class PlansController {

    private final AutomationPlanManager planManager = new AutomationPlanManager();

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

    /**
     * Diagram: "Utworzenie nowego planu" i koniec.
     * Tworzymy pusty plan (reguły można dodać później w "Dodanie reguły").
     */
    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody CreatePlanRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return ResponseEntity.badRequest().body("Niepoprawna nazwa planu.");
        }
        Integer id = planManager.createPlan(req.name, new ArrayList<>());
        if (id == null) return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się utworzyć planu.");
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
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

        // Dopasuj konstruktor AutomationRule do Twojej implementacji.
        AutomationRule rule = new AutomationRule(req.deviceId, req.states, req.timeWindow);

        boolean ok = planManager.addRule(planId, rule);
        return ok ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono planu.");
    }
}
