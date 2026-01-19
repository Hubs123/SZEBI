package pl.szebi.optimization.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.szebi.optimization.*;

import java.util.List;

/**
 * Kontroler REST obsługujący moduł optymalizacji energetycznej.
 * Integruje logikę biznesową z interfejsem użytkownika.
 */
@RestController
@RequestMapping("/api/optimization")
@CrossOrigin(origins = "http://localhost:3000") // Umożliwia komunikację z aplikacją React.
public class OptimizationController {

    private final OptimizationManager optimizationManager;
    private final OptimizationPlanRepository planRepo;

    /**
     * Konstruktor wstrzykujący menedżera optymalizacji.
     * Repozytorium jest inicjalizowane wewnętrznie dla spójności z modelem danych.
     */

    public OptimizationController() {
        this.optimizationManager = new OptimizationManager();
        this.planRepo = new OptimizationPlanRepository();
    }

    /**
     * Pobiera listę wszystkich planów zapisanych w systemie.
     * Dostępne zarówno dla Mieszkańca, jak i Administratora.
     */
    @GetMapping("/plans")
    public List<OptimizationPlan> getPlans() {
        return planRepo.findAll(); // Pobiera dane z bazy poprzez repozytorium.
    }

    /**
     * Endpoint dla Administratora: Generuje nowy plan na podstawie wybranej strategii.
     * Realizuje przypadek użycia "Generowanie planu optymalizacji" i "Wybór parametrów".
     */
    @PostMapping("/generate")
    public OptimizationPlan generatePlan(@RequestParam Integer userId, @RequestParam String strategyType) {
//        OptimizationStrategy strategy;
//
//        // Mapowanie tekstowego parametru na konkretną klasę strategii.
//        switch (strategyType) {
//            case "Costs_reduction" -> strategy = new CostReductionStrategy(); //
//            case "Co2_reduction" -> strategy = new Co2ReductionStrategy();   //
//            case "Load_reduction" -> strategy = new LoadReductionStrategy(); //
//            default -> throw new IllegalArgumentException("Nieznany typ strategii: " + strategyType);
//        }

//        OptimizationPlan newPlan = new OptimizationPlan(userId, strategy); // Tworzy obiekt planu w statusie Draft.

        // Inicjalizacja planu domyślnymi regułami automatyzacji przed zapisem.
//        newPlan.setRules(OptimizationMockData.generateMockAutomationRules());
//
//        planRepo.save(newPlan); // Zapisuje plan do bazy danych.
//        return newPlan;
        return null;
    }

    /**
     * Uruchamia wybrany plan optymalizacji.
     * Powoduje zmianę statusu na 'Active' i start dedykowanego wątku symulacji.
     */
    @PostMapping("/plans/{id}/run")
    public boolean runPlan(@PathVariable Integer id) {
        // Wywołuje logikę managera odpowiedzialną za Thread.start().
        return optimizationManager.runPlan(id);
    }

    /**
     * Zatrzymuje działający plan optymalizacji.
     * Zmienia status na 'Stopped', co przerywa pętlę simulationLoop w wątku.
     */
    @PostMapping("/plans/{id}/stop")
    public boolean stopPlan(@PathVariable Integer id) {
        // Wywołuje logikę managera odpowiedzialną za zatrzymanie wątku.
        return optimizationManager.stopPlan(id);
    }

    /**
     * Zmienia nazwę istniejącego planu.
     * Funkcja zarządzania planami dedykowana dla Administratora.
     */
    @PatchMapping("/plans/{id}/rename")
    public boolean renamePlan(@PathVariable Integer id, @RequestBody String newName) {
        // Oczyszczanie otrzymanego ciągu znaków z ewentualnych cudzysłowów JSON.
        String cleanedName = newName.replace("\"", "");
        return planRepo.updateName(id, cleanedName); // Trwały zapis nowej nazwy w DB.
    }
}