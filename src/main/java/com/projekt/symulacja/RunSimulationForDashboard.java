package com.projekt.symulacja;

import java.time.LocalDate;
import java.util.List;

/**
 * Prosty skrypt do uruchomienia symulacji i zapisania wyników.
 * Użyj tego aby wygenerować nowe dane dla Frontend Dashboard.
 */
public class RunSimulationForDashboard {

    public static void main(String[] args) {
        try {
            System.out.println("=".repeat(60));
            System.out.println("Uruchamianie symulacji dla Dashboard Frontend");
            System.out.println("=".repeat(60));

            // 1. Utworzenie ustawień systemu
            Settings settings = new Settings();
            settings.setPanelPower(5.0);        // 5 kW mocy paneli
            settings.setBatteryCapacity(100.0); // 100 kWh pojemności magazynu

            System.out.println("\n[1] Ustawienia systemu:");
            System.out.println("    Moc paneli: " + settings.getPanelPower() + " kW");
            System.out.println("    Pojemność akumulatora: " + settings.getBatteryCapacity() + " kWh");

            // 2. Utworzenie managera symulacji
            SimulationManager manager = new SimulationManager(settings);

            // 3. Wykonanie symulacji dla dzisiejszej daty
            LocalDate today = LocalDate.now();
            System.out.println("\n[2] Uruchamianie symulacji dla daty: " + today);

            List<SimulationRecord> records = manager.simulateDay(today);

            System.out.println("\n[3] Wygenerowano " + records.size() + " rekordów symulacji:");
            System.out.println();
            System.out.println("┌─────────┬──────────────┬──────────────┬──────────────┬──────────────┐");
            System.out.println("│ Okres   │ Produkcja PV │ Grid Consume │ Grid Feed-In │ Poziom Bat.  │");
            System.out.println("├─────────┼──────────────┼──────────────┼──────────────┼──────────────┤");

            for (SimulationRecord record : records) {
                System.out.printf("│ %7d │ %10.2f kWh │ %10.2f kWh │ %10.2f kWh │ %10.2f kWh │%n",
                    record.getPeriodNumber(),
                    record.getPvProduction(),
                    record.getGridConsumption(),
                    record.getGridFeedIn(),
                    record.getBatteryLevel()
                );
            }

            System.out.println("└─────────┴──────────────┴──────────────┴──────────────┴──────────────┘");

            // 4. Obliczenie statystyk
            double totalGridConsumption = records.stream()
                .mapToDouble(SimulationRecord::getGridConsumption)
                .sum();
            double avgGridConsumption = records.stream()
                .mapToDouble(SimulationRecord::getGridConsumption)
                .average()
                .orElse(0.0);
            double minGridConsumption = records.stream()
                .mapToDouble(SimulationRecord::getGridConsumption)
                .min()
                .orElse(0.0);
            double maxGridConsumption = records.stream()
                .mapToDouble(SimulationRecord::getGridConsumption)
                .max()
                .orElse(0.0);

            System.out.println("\n[4] Statystyki Grid Consumption (to samo co w Dashboard):");
            System.out.println("    Średnie zużycie: " + String.format("%.2f", avgGridConsumption) + " kWh");
            System.out.println("    Minimum:         " + String.format("%.2f", minGridConsumption) + " kWh");
            System.out.println("    Maximum:         " + String.format("%.2f", maxGridConsumption) + " kWh");
            System.out.println("    Całkowite:       " + String.format("%.2f", totalGridConsumption) + " kWh");

            System.out.println("\n[5] ✓ Symulacja zakończona pomyślnie!");
            System.out.println("    Wyniki są teraz dostępne w SimulationManager.getSimulationResults()");
            System.out.println("    Frontend Dashboard powinien pokazać powyższe wartości.");

            System.out.println("\n[6] Aby zobaczyć dane w Frontend:");
            System.out.println("    1. Uruchom Spring Boot: mvn spring-boot:run");
            System.out.println("    2. Uruchom Python backend: python -m app.main");
            System.out.println("    3. Uruchom Frontend: npm start");
            System.out.println("    4. Otwórz http://localhost:3000");

            System.out.println("\n" + "=".repeat(60));

        } catch (Exception e) {
            System.err.println("\n✗ Błąd podczas symulacji: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

