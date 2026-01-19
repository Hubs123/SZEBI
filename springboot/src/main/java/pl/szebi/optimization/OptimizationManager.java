package pl.szebi.optimization;


import org.springframework.stereotype.Service; // Opcjonalnie, jeśli używasz Springa
import java.util.Date;
import pl.szebi.time.TimeControl;
import java.util.List;
import java.util.Optional;

@Service
public class OptimizationManager {
    private final OptimizationPlanRepository planRepo;
    private Thread currentSimulationThread;

    OptimizationManager() {

        planRepo = new OptimizationPlanRepository();
    }

    // Uruchomienie planu z ustawieniem wątku oczekującego na dane symulacji z bazy danych
    public boolean runPlan(Integer userId, Integer planId) {
        OptimizationPlan plan = planRepo.findById(planId);
        if (plan == null) return false;

        // Zatrzymanie innego działającego planu
        if (this.currentSimulationThread != null && this.currentSimulationThread.isAlive()) {
            stopPlan(userId, planId);
        }

        // Ustaw status ACTIVE
        plan.setStatus(PlanStatus.Active);
        planRepo.save(plan);

        // Uruchom WĄTEK DEDYKOWANY dla tego planu
        Runnable task = () -> simulationLoop(planId);

        this.currentSimulationThread = new Thread(task);
        this.currentSimulationThread.setName("Plan-Thread-" + planId);
        this.currentSimulationThread.start(); // <--- TU STARTUJE OSOBNY WĄTEK

        System.out.println("Uruchomiono wątek dla planu: " + planId);
        return true;
    }

    // Logika wątku dla uruchomionego planu
    // Sprawdzanie, czy dane symulacji istnieją i przeliczenie ustawień urządzeń tylko wtedy
    // Aktualna data symulacji pobierana z zegara TimeControl z pakietu pl.szebi.time
    // Pętla kończona kiedy plan zostanie zatrzymany
    private void simulationLoop(Integer planId) {
        // Zmienna do zapamiętania, kiedy ostatnio robiliśmy obliczenia
        Date lastProcessedDate = null;
        OptimizationData optimizationData = new OptimizationData();

        while (true) {
            try {
                // Czytamy czas symulacji RAZ na obrót pętli
                Date currentSimDate = Date.from(TimeControl.now());

                // Jeśli czas symulacji się nie zmienił od ostatnich obliczeń, to czekamy.
                if (lastProcessedDate != null && lastProcessedDate.equals(currentSimDate)) {
                    Thread.sleep(1000); // Odpocznij 1s (realnego czasu) zanim znowu sprawdzisz zegar
                    continue; // Wróć na początek pętli
                }

                // Sprawdzenie statusu planu (dopiero gdy czas się zmienił lub to pierwszy raz)
                OptimizationPlan plan = planRepo.findById(planId);

                if (plan == null || plan.getStatus() != PlanStatus.Active) {
                    System.out.println("Plan zakończony lub zatrzymany. Zamykam wątek.");
                    break;
                }

                optimizationData.loadFromDatabase(currentSimDate);

                // 4. Sprawdzenie danych (tylko jeśli mamy "nową" datę w symulacji)
                if (has6RecordsForDate(currentSimDate)) {
                    System.out.println("   [SIM] Dane kompletne dla: " + currentSimDate + ". Przeliczam...");

                    OptimizationData data = loadFromDatabase(currentSimDate);
                    plan.getStrategy().calculate(plan, data, getCurrentRules());

                    // --- WAŻNE: Oznaczamy tę datę jako "załatwioną" ---
                    lastProcessedDate = currentSimDate;

                    System.out.println("   [SIM] Obliczenia zakończone. Czekam na zmianę czasu.");

                } else {
                    // Brak danych - logujemy to rzadziej lub czekamy
                    // Tutaj NIE ustawiamy lastProcessedDate, bo chcemy spróbować znowu za chwilę
                    System.out.println("   [WAIT] Brak kompletu danych dla: " + currentSimDate);

                    Thread.sleep(2000); // Czekamy 2s na pojawienie się danych w bazie
                }

            } catch (InterruptedException e) {
                System.out.println("Wątek przerwany (Interrupt).");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Błąd w pętli symulacji: " + e.getMessage());
                e.printStackTrace();
                // Ważne: Sleep po błędzie, żeby nie spamować logów w pętli nieskończonej przy awarii DB
                try { Thread.sleep(5000); } catch (InterruptedException ex) { break; }
            }
        }
    }

    // --- METODA WYWOŁYWANA Z KONTROLERA ---
    public boolean stopPlan(Integer userId, Integer planId) {
        OptimizationPlan plan = planRepo.findById(planId);
        if (plan != null) {
            // 1. Zmieniamy status na STOPPED
            plan.setStatus(PlanStatus.Stopped);
            planRepo.save(plan);
            System.out.println("Zatrzymano plan: " + planId + ". Wątek powinien się zakończyć za chwilę.");
            return true;
        }
        return false;
    }

    // getPlans, removePlan - implementacja analogiczna do repo
}