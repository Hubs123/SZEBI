package pl.szebi.optimization;

import org.springframework.stereotype.Service;
import pl.szebi.time.TimeControl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OptimizationManager {

    private final OptimizationPlanRepository planRepo;
    private final ExecutorService executorService;
    private final Map<Integer, Future<?>> activeTasks;

    public OptimizationManager() {
        this.planRepo = new OptimizationPlanRepository();
        this.executorService = Executors.newCachedThreadPool();
        this.activeTasks = new ConcurrentHashMap<>();
    }

    public OptimizationPlan generatePlan(Integer userId, String strategyTypeStr) {
        OptimizationStrategyType type;
        try {
            type = OptimizationStrategyType.valueOf(strategyTypeStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Nieznany typ strategii: " + strategyTypeStr);
            return null;
        }

        OptimizationStrategy strategy = switch (type) {
            case Costs_reduction -> new CostReductionStrategy();
            case Load_reduction -> new LoadReductionStrategy();
        };

        OptimizationPlan plan = new OptimizationPlan(null, userId, strategy);
        plan.setName("Nowy Plan (" + strategyTypeStr + ")");

        if (planRepo.save(plan)) {
            return plan;
        }
        return null;
    }

    public boolean runPlan(Integer planId) {
        OptimizationPlan plan = planRepo.findById(planId);
        if (plan == null) return false;

        if (activeTasks.containsKey(planId)) {
            stopPlan(planId);
        }

        plan.setStatus(PlanStatus.Active);
        planRepo.save(plan);

        Future<?> task = executorService.submit(() -> simulationLoop(planId));
        activeTasks.put(planId, task);

        System.out.println("Uruchomiono plan ID: " + planId);
        return true;
    }

    public boolean stopPlan(Integer planId) {
        Future<?> task = activeTasks.remove(planId);
        if (task != null) {
            task.cancel(true);
        }

        OptimizationPlan plan = planRepo.findById(planId);
        if (plan != null) {
            plan.setStatus(PlanStatus.Stopped);
            planRepo.save(plan);
            System.out.println("Zatrzymano plan ID: " + planId);
            return true;
        }
        return false;
    }

    public boolean deletePlan(Integer planId) {
        stopPlan(planId);
        return planRepo.delete(planId);
    }

    private void simulationLoop(Integer planId) {
        OptimizationData data = new OptimizationData();
        Date lastProcessed = null;
        String threadName = Thread.currentThread().getName();

        System.out.println("[" + threadName + "] Start pętli dla planu: " + planId);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 2. Unikanie wielokrotnego przetwarzania tej samej chwili
                    if (lastProcessed != null && lastProcessed.equals(data.getTimestamp())) {
                        TimeUnit.MILLISECONDS.sleep(10000);
                        continue;
                    }

                    // 3. Sprawdzenie czy plan nadal istnieje i jest aktywny
                    OptimizationPlan plan = planRepo.findById(planId);
                    if (plan == null || plan.getStatus() != PlanStatus.Active) {
                        System.out.println("[" + threadName + "] Plan nieaktywny lub usunięty. Koniec.");
                        break;
                    }

                    // 4. Pobranie danych i obliczenia
                    if (data.loadFromDatabase()) {
                        System.out.println("[SIM " + planId + "] Przeliczanie dla: " + data.getTimestamp());

                        boolean success = plan.getStrategy().calculate(plan, data, plan.getRules());

                        if (success) {
                            planRepo.save(plan);
                        }
                    }

                    lastProcessed = data.getTimestamp();

                } catch (InterruptedException ie) {
                    // Przerwanie wątku (np. przy stopPlan)
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Błąd wewnątrz pętli nie powinien zabijać całego wątku (chyba że to błąd krytyczny)
                    System.err.println("[" + threadName + "] Błąd w iteracji symulacji: " + e.getMessage());
                    e.printStackTrace();
                    TimeUnit.SECONDS.sleep(2); // Odczekaj chwilę przed ponowną próbą
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            activeTasks.remove(planId);
            System.out.println("[" + threadName + "] Koniec pętli dla planu: " + planId);
        }
    }
}