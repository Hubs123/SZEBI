package pl.szebi.symulacja;

import java.time.LocalDate;
import java.util.List;

/**
 * Przykładowa klasa demonstrująca użycie modułu symulacji i akwizycji danych.
 */
public class SimulationExample {
    
    public static void main(String[] args) {
        try {
            // 1. Utworzenie ustawień systemu
            Settings settings = new Settings();
            settings.setPanelPower(5.0); // 5 kW mocy paneli
            settings.setBatteryCapacity(100.0); // 100 kWh pojemności magazynu
            
            // Alternatywnie można użyć konstruktora:
            // Settings settings = new Settings(5.0, 10.0);
            
            // 2. Utworzenie managera symulacji
            SimulationManager manager = new SimulationManager(settings);
            
            // 3. Wykonanie symulacji dla wybranej daty
            LocalDate date = LocalDate.now();
            System.out.println("Rozpoczynam symulację dla daty: " + date);
            
            List<SimulationRecord> records = manager.simulateDay(date);
            System.out.println("Wygenerowano " + records.size() + " rekordów symulacji");
            
            // 4. Zapis wyników do bazy danych
            DataRepository repository = new DataRepository();
            boolean saved = repository.saveAll(records);
            
            if (saved) {
                System.out.println("Wszystkie rekordy zostały zapisane do bazy danych");
            } else {
                System.out.println("Wystąpił problem podczas zapisywania rekordów");
            }
            
            // 5. Pobranie danych z bazy dla wybranej daty
            List<SimulationRecord> retrievedRecords = repository.findByDate(date);
            System.out.println("Pobrano " + retrievedRecords.size() + " rekordów z bazy danych");
            
            // 6. Wyświetlenie przykładowych wyników
            if (!retrievedRecords.isEmpty()) {
                System.out.println("\nPrzykładowe wyniki symulacji:");
                for (SimulationRecord record : retrievedRecords) {
                    System.out.println(String.format(
                        "Okres %d: Produkcja PV: %.2f kWh, " +
                        "Zmagazynowane: %.2f kWh, Poziom akumulatora: %.2f kWh, " +
                        "Grid consumption: %.2f kWh, Grid feed-in: %.2f kWh",
                        record.getPeriodNumber(),
                        record.getPvProduction(),
                        record.getEnergyStored(),
                        record.getBatteryLevel(),
                        record.getGridConsumption(),
                        record.getGridFeedIn()
                    ));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Błąd podczas symulacji: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


