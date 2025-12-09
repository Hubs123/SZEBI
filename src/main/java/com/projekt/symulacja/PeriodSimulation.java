package com.projekt.symulacja;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Klasa odpowiedzialna za symulację jednego okresu czasowego (4h).
 * Oblicza produkcję energii PV, magazynowanie energii oraz grid consumption i feed-in.
 */
public class PeriodSimulation {
    private static final int PERIOD_DURATION_HOURS = 4;
    
    /**
     * Symuluje jeden okres czasowy (4h).
     * 
     * @param simulationDate data symulacji
     * @param periodNumber numer okresu (1-6)
     * @param sunlightIntensity nasłonecznienie (0.0 - 1.0)
     * @param householdConsumption zużycie energii w gospodarstwie (kWh)
     * @param initialBatteryLevel początkowy poziom akumulatora (kWh)
     * @param settings ustawienia systemu (moc paneli, pojemność magazynu)
     * @return rekord symulacji dla tego okresu
     * @throws IllegalArgumentException jeśli parametry są nieprawidłowe
     */
    public static SimulationRecord simulate(
            LocalDate simulationDate,
            Integer periodNumber,
            Double sunlightIntensity,
            Double householdConsumption,
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
        if (householdConsumption == null || householdConsumption < 0.0) {
            throw new IllegalArgumentException("Zużycie energii nie może być ujemne");
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
        
        // Zawsze część zużycia pochodzi z sieci (10-20% jako rezerwa/straty)
        // To zapewnia, że grid consumption zawsze będzie > 0
        Double baseGridConsumption = householdConsumption * 0.15; // 15% zużycia zawsze z sieci
        Double effectiveConsumption = householdConsumption - baseGridConsumption;
        
        // Obliczanie dostępnej energii (produkcja + energia z akumulatora)
        Double availableEnergy = pvProduction + initialBatteryLevel;
        
        // Obliczanie energii zmagazynowanej, grid consumption i grid feed-in
        Double energyStored = 0.0;
        Double gridConsumption = baseGridConsumption; // Zawsze minimum 15% zużycia
        Double gridFeedIn = 0.0;
        Double finalBatteryLevel;
        
        if (availableEnergy >= effectiveConsumption) {
            // Mamy nadmiar energii dla efektywnego zużycia
            Double surplus = availableEnergy - effectiveConsumption;
            
            // Najpierw ładujemy akumulator
            Double maxBatteryCharge = settings.getBatteryCapacity() - initialBatteryLevel;
            if (surplus > maxBatteryCharge) {
                // Akumulator pełny, reszta idzie do sieci (grid feed-in)
                energyStored = maxBatteryCharge;
                gridFeedIn = surplus - maxBatteryCharge;
                finalBatteryLevel = settings.getBatteryCapacity();
            } else {
                // Cały nadmiar idzie do akumulatora
                energyStored = surplus;
                gridFeedIn = 0.0;
                finalBatteryLevel = initialBatteryLevel + surplus;
            }
        } else {
            // Brakuje energii dla efektywnego zużycia, pobieramy z akumulatora i/lub z sieci
            Double deficit = effectiveConsumption - availableEnergy;
            if (initialBatteryLevel >= deficit) {
                // Akumulator wystarczy dla deficytu
                finalBatteryLevel = initialBatteryLevel - deficit;
                energyStored = 0.0;
                // gridConsumption pozostaje baseGridConsumption (już ustawione)
            } else {
                // Akumulator nie wystarczy, trzeba pobrać więcej z sieci
                finalBatteryLevel = 0.0;
                energyStored = 0.0;
                gridConsumption = baseGridConsumption + (deficit - initialBatteryLevel); // Dodatkowa energia z sieci
            }
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
                gridConsumption,
                gridFeedIn,
                settings.getPanelPower(),
                settings.getBatteryCapacity()
        );
        
        return record;
    }
}
