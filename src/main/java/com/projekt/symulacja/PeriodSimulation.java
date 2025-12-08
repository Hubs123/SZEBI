package com.projekt.symulacja;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Klasa odpowiedzialna za symulację jednego okresu czasowego (4h).
 * Oblicza produkcję energii PV i magazynowanie energii.
 */
public class PeriodSimulation {
    private static final int PERIOD_DURATION_HOURS = 4;
    
    /**
     * Symuluje jeden okres czasowy (4h).
     * 
     * @param simulationDate data symulacji
     * @param periodNumber numer okresu (1-6)
     * @param sunlightIntensity nasłonecznienie (0.0 - 1.0)
     * @param initialBatteryLevel początkowy poziom akumulatora (kWh)
     * @param settings ustawienia systemu (moc paneli, pojemność magazynu)
     * @return rekord symulacji dla tego okresu
     * @throws IllegalArgumentException jeśli parametry są nieprawidłowe
     */
    public static SimulationRecord simulate(
            LocalDate simulationDate,
            Integer periodNumber,
            Double sunlightIntensity,
            Double initialBatteryLevel,
            Settings settings) {
        
        // Walidacja parametrów
        if (simulationDate == null) {
            throw new IllegalArgumentException("Data symulacji nie może być null");
        }
        if (periodNumber == null || periodNumber < 1 || periodNumber > 6) {
            throw new IllegalArgumentException("Numer okresu musi być w zakresie 1-6");
        }
        if (sunlightIntensity == null || sunlightIntensity < 0.0 || sunlightIntensity > 1.0) {
            throw new IllegalArgumentException("Nasłonecznienie musi być w zakresie 0.0 - 1.0");
        }
        if (initialBatteryLevel == null || initialBatteryLevel < 0.0) {
            throw new IllegalArgumentException("Początkowy poziom akumulatora nie może być ujemny");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Ustawienia nie mogą być null");
        }
        
        settings.validate();
        
        // Obliczanie daty rozpoczęcia i zakończenia okresu
        LocalDateTime periodStart = simulationDate.atStartOfDay()
                .plusHours((periodNumber - 1) * PERIOD_DURATION_HOURS);
        LocalDateTime periodEnd = periodStart.plusHours(PERIOD_DURATION_HOURS);
        
        // Obliczanie produkcji energii PV
        // Produkcja = moc paneli * czas * nasłonecznienie
        Double pvProduction = settings.getPanelPower() * PERIOD_DURATION_HOURS * sunlightIntensity;
        
        // Obliczanie energii zmagazynowanej
        // Cała wyprodukowana energia idzie do akumulatora (do momentu jego zapełnienia)
        Double maxBatteryCharge = settings.getBatteryCapacity() - initialBatteryLevel;
        Double energyStored;
        Double finalBatteryLevel;
        
        if (pvProduction > maxBatteryCharge) {
            // Akumulator pełny, reszta energii nie jest magazynowana
            energyStored = maxBatteryCharge;
            finalBatteryLevel = settings.getBatteryCapacity();
        } else {
            // Cała energia idzie do akumulatora
            energyStored = pvProduction;
            finalBatteryLevel = initialBatteryLevel + pvProduction;
        }
        
        // Tworzenie rekordu symulacji
        SimulationRecord record = new SimulationRecord(
                simulationDate,
                periodNumber,
                periodStart,
                periodEnd,
                sunlightIntensity,
                pvProduction,
                energyStored,
                finalBatteryLevel,
                settings.getPanelPower(),
                settings.getBatteryCapacity()
        );
        
        return record;
    }
}
