package pl.szebi.optimization;


import org.springframework.stereotype.Service; // Opcjonalnie, jeśli używasz Springa

import java.sql.SQLException;
import java.util.Date;
import pl.szebi.time.TimeControl;

@Service
public class OptimizationManager {
    private final OptimizationPlanRepository planRepo;
    private Thread currentSimulationThread;

    public OptimizationManager() {
        planRepo = new OptimizationPlanRepository();
    }

//    public OptimizationPlan generatePlan(int userId, Date startDate, Date endDate, String strategyType) {
//        Connection conn = Db.conn;
//        int newId = 1; // Domyślne ID, jeśli tabela jest pusta
//
//        // KROK 1: Pobranie następnego dostępnego ID
//        String getMaxIdSql = "SELECT MAX(id) FROM optimization_plan";
//
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(getMaxIdSql)) {
//
//            if (rs.next()) {
//                // Pobieramy MAX(id). Jeśli tabela pusta, zwróci 0 (lub null w zależnosci od drivera),
//                // więc +1 da nam ID = 1.
//                int maxId = rs.getInt(1);
//                newId = maxId + 1;
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null; // Błąd przy pobieraniu ID
//        }
//
//        // KROK 2: Utworzenie i zapisanie nowego planu
//        String insertSql = "INSERT INTO optimization_plan " +
//                "(id, user_id, start_date, end_date, strategy, status) " +
//                "VALUES (?, ?, ?, ?, ?, ?)";
//
//        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
//
//            pstmt.setInt(1, newId);
//            pstmt.setInt(2, userId);
//            // Konwersja java.util.Date na java.sql.Timestamp
//            pstmt.setTimestamp(3, new java.sql.Timestamp(startDate.getTime()));
//            pstmt.setTimestamp(4, new java.sql.Timestamp(endDate.getTime()));
//            pstmt.setString(5, strategyType);
//            pstmt.setString(6, "DRAFT"); [cite_start]// Domyślny status po utworzeniu [cite: 99]
//
//            int rowsAffected = pstmt.executeUpdate();
//
//            if (rowsAffected > 0) {
//                // Tworzymy obiekt Java do zwrócenia w aplikacji
//                // (Zakładam, że masz odpowiedni konstruktor w OptimizationPlan)
//                OptimizationPlan plan = new OptimizationPlan();
//                plan.setId(newId);
//                plan.setUserId(userId);
//                // Tutaj logika ustawiania strategii na podstawie stringa
//                // plan.setStrategy(StrategyFactory.get(strategyType));
//                return plan;
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return null; // Zwracamy null jeśli zapis się nie powiódł
//    }

    // Uruchomienie planu z ustawieniem wątku oczekującego na dane symulacji z bazy danych
    public boolean runPlan(Integer planId) {
        OptimizationPlan plan = planRepo.findById(planId);
        if (plan == null) return false;

        // Zatrzymanie innego działającego planu
        if (this.currentSimulationThread != null && this.currentSimulationThread.isAlive()) {
            stopPlan(planId);
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
                System.out.println(plan);

                if (plan == null || plan.getStatus() != PlanStatus.Active) {
                    System.out.println("Plan zakończony lub zatrzymany. Zamykam wątek.");
                    break;
                }

                optimizationData.loadFromDatabase();

                // 4. Sprawdzenie danych (tylko jeśli mamy "nową" datę w symulacji)

                System.out.println("   [SIM] Dane kompletne dla: " + currentSimDate + ". Przeliczam...");

                plan.getStrategy().calculate(plan, optimizationData, plan.getRules());

                // --- WAŻNE: Oznaczamy tę datę jako "załatwioną" ---
                lastProcessedDate = currentSimDate;

                System.out.println("   [SIM] Obliczenia zakończone. Czekam na zmianę czasu.");



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

//    private void loadRulesFromDatabase()

    // --- METODA WYWOŁYWANA Z KONTROLERA ---
    public boolean stopPlan(Integer planId) {
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