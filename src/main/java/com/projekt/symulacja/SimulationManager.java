package com.projekt.symulacja;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Klasa zarządzająca symulacją dobową.
 * Koordynuje symulację 6 okresów czasowych (4h każdy).
 */
public class SimulationManager {
    private static final int PERIODS_PER_DAY = 6;
    
    private Settings settings;
    private Random random;
    
    // Tablica do przechowywania wyników symulacji
    private static SimulationRecord[] simulationResults = new SimulationRecord[PERIODS_PER_DAY];
    
    public SimulationManager() {
        this.settings = new Settings();
        this.random = new Random();
    }
    
    public SimulationManager(Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Ustawienia nie mogą być null");
        }
        this.settings = settings;
        this.random = new Random();
    }
    
    /**
     * Generuje symulację dobową dla podanej daty.
     * 
     * @param date data symulacji
     * @return lista rekordów symulacji (6 rekordów - po jednym na okres)
     * @throws IllegalStateException jeśli ustawienia są nieprawidłowe
     */
    public List<SimulationRecord> simulateDay(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data nie może być null");
        }
        
        settings.validate();
        
        List<SimulationRecord> records = new ArrayList<>();
        Double currentBatteryLevel = 0.0; // Początkowy poziom akumulatora
        
        for (int period = 1; period <= PERIODS_PER_DAY; period++) {
            // Generowanie parametrów wejściowych dla okresu
            Double sunlightIntensity = generateSunlightIntensity(period);
            Double householdConsumption = generateHouseholdConsumption(period);
            
            // Symulacja okresu
            SimulationRecord record = PeriodSimulation.simulate(
                    date,
                    period,
                    sunlightIntensity,
                    householdConsumption,
                    currentBatteryLevel,
                    settings
            );
            
            records.add(record);
            
            // Zapisanie wyniku do tablicy
            simulationResults[period - 1] = record;
            
            // Aktualizacja poziomu akumulatora dla następnego okresu
            currentBatteryLevel = record.getBatteryLevel();
        }
        
        return records;
    }
    
    /**
     * Zwraca tablicę z wynikami ostatniej symulacji.
     * Tablica zawiera 6 rekordów (po jednym na każdy okres).
     * 
     * @return tablica z wynikami symulacji lub null jeśli symulacja nie została jeszcze wykonana
     */
    public static SimulationRecord[] getSimulationResults() {
        return simulationResults;
    }
    
    /**
     * Zwraca tablicę z wynikami ostatniej symulacji jako kopię.
     * Bezpieczniejsza wersja - zwraca kopię tablicy, więc modyfikacje nie wpłyną na oryginał.
     * 
     * @return kopia tablicy z wynikami symulacji lub null jeśli symulacja nie została jeszcze wykonana
     */
    public static SimulationRecord[] getSimulationResultsCopy() {
        if (simulationResults == null) {
            return null;
        }
        SimulationRecord[] copy = new SimulationRecord[simulationResults.length];
        System.arraycopy(simulationResults, 0, copy, 0, simulationResults.length);
        return copy;
    }
    
    /**
     * Czyści tablicę z wynikami symulacji.
     */
    public static void clearSimulationResults() {
        simulationResults = new SimulationRecord[PERIODS_PER_DAY];
    }
    
    /**
     * Generuje nasłonecznienie dla danego okresu.
     * Symuluje naturalny cykl dobowy - więcej słońca w środku dnia.
     * 
     * @param periodNumber numer okresu (1-6)
     * @return nasłonecznienie w zakresie 0.0 - 1.0
     */
    private Double generateSunlightIntensity(Integer periodNumber) {
        // Wzór symulujący naturalny cykl dobowy
        // Okresy: 1 (0-4h), 2 (4-8h), 3 (8-12h), 4 (12-16h), 5 (16-20h), 6 (20-24h)
        double baseIntensity;
        
        switch (periodNumber) {
            case 1: // 0-4h (noc)
                baseIntensity = 0.0;
                break;
            case 2: // 4-8h (wschód słońca)
                baseIntensity = 0.3;
                break;
            case 3: // 8-12h (rano)
                baseIntensity = 0.7;
                break;
            case 4: // 12-16h (południe)
                baseIntensity = 1.0;
                break;
            case 5: // 16-20h (popołudnie)
                baseIntensity = 0.6;
                break;
            case 6: // 20-24h (wieczór/noc)
                baseIntensity = 0.0;
                break;
            default:
                baseIntensity = 0.5;
        }
        
        // Dodanie losowej zmienności (±20%)
        double variation = (random.nextDouble() - 0.5) * 0.4;
        double intensity = baseIntensity + variation;
        
        // Ograniczenie do zakresu 0.0 - 1.0
        return Math.max(0.0, Math.min(1.0, intensity));
    }
    
    /**
     * Generuje zużycie energii w gospodarstwie dla danego okresu.
     * Symuluje większe zużycie w godzinach aktywności (dzień 6-22h).
     * Analogicznie do pliku Python: więcej w dzień (2.5-4.5 kWh), mniej w nocy (0.8-1.2 kWh).
     * 
     * @param periodNumber numer okresu (1-6)
     * @return zużycie energii w kWh
     */
    private Double generateHouseholdConsumption(Integer periodNumber) {
        // Okresy: 1 (0-4h), 2 (4-8h), 3 (8-12h), 4 (12-16h), 5 (16-20h), 6 (20-24h)
        // Symulacja podobna do Python: więcej w dzień (6-22h), mniej w nocy
        double baseConsumption;
        
        switch (periodNumber) {
            case 1: // 0-4h (noc)
                baseConsumption = 0.8 + (periodNumber % 3) * 0.2; // 0.8 - 1.2 kWh
                break;
            case 2: // 4-8h (wschód słońca) - przejście z nocy do dnia
                baseConsumption = 2.0; // średnie zużycie
                break;
            case 3: // 8-12h (rano) - dzień
                baseConsumption = 2.5 + (periodNumber % 5) * 0.5; // 2.5 - 4.5 kWh
                break;
            case 4: // 12-16h (południe) - dzień
                baseConsumption = 2.5 + (periodNumber % 5) * 0.5; // 2.5 - 4.5 kWh
                break;
            case 5: // 16-20h (popołudnie) - dzień
                baseConsumption = 2.5 + (periodNumber % 5) * 0.5; // 2.5 - 4.5 kWh
                break;
            case 6: // 20-24h (wieczór/noc) - przejście z dnia do nocy
                baseConsumption = 1.5; // średnie zużycie
                break;
            default:
                baseConsumption = 2.0;
        }
        
        // Dodanie losowej zmienności (±20%) - analogicznie do Python
        double variation = random.nextDouble() * 0.4 + 0.8; // 0.8 - 1.2
        double consumption = baseConsumption * variation;
        
        // Zapewnienie, że zużycie nie jest ujemne
        return Math.max(0.1, consumption);
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public void setSettings(Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Ustawienia nie mogą być null");
        }
        this.settings = settings;
    }
}
