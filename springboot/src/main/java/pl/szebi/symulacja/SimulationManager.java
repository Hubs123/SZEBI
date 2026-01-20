package pl.szebi.symulacja;

import pl.szebi.sterowanie.Device;
import pl.szebi.sterowanie.DeviceManager;
import pl.szebi.sterowanie.DeviceType;
import pl.szebi.alerts.Alert;
import pl.szebi.alerts.AlertManager;
import java.sql.Timestamp;

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
            
            // Sprawdzenie czy należy wygenerować alert
            checkAndGenerateAlert(record);
            
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
     * Oblicza zużycie energii na podstawie wszystkich urządzeń z modułu sterowania.
     * Każde urządzenie ma przypisaną moc na podstawie typu. Urządzenia włączone (power=1.0f)
     * zużywają energię, wyłączone (power=0.0f) nie zużywają.
     * 
     * @param periodDurationHours czas trwania okresu w godzinach
     * @return całkowite zużycie energii w kWh dla danego okresu
     */
    private Double calculateHouseholdConsumptionFromDevices(double periodDurationHours) {
        DeviceManager deviceManager = new DeviceManager();
        List<Device> devices = deviceManager.listDevices();
        
        double totalConsumption = 0.0;
        
        for (Device device : devices) {
            // Sprawdzenie czy urządzenie jest włączone
            if (!device.isOn()) {
                continue; // Urządzenie wyłączone nie zużywa energii
            }
            
            // Przypisanie mocy (kW) na podstawie typu urządzenia
            double devicePower = getDevicePowerConsumption(device.getType());
            
            // Zużycie energii = moc * czas (kWh)
            double deviceConsumption = devicePower * periodDurationHours;
            totalConsumption += deviceConsumption;
        }
        
        // Zapewnienie minimalnego zużycia (np. oświetlenie podstawowe, lodówka)
        // Minimum 0.5 kWh na okres (4h) dla podstawowych urządzeń
        double baseConsumption = 0.5;
        return Math.max(baseConsumption, totalConsumption);
    }
    
    /**
     * Zwraca moc zużycia (kW) dla danego typu urządzenia.
     * 
     * @param deviceType typ urządzenia
     * @return moc zużycia w kW
     */
    private double getDevicePowerConsumption(DeviceType deviceType) {
        switch (deviceType) {
            case thermometer:
                return 0.001; // 1W - bardzo małe zużycie
            case smokeDetector:
                return 0.005; // 5W - małe zużycie
            case noSimulation:
                // Dla urządzeń typu noSimulation (żarówki, okna smart itp.)
                // używamy średniego zużycia, np. żarówka LED ~10W
                return 0.010; // 10W
            default:
                return 0.010; // Domyślne 10W dla nieznanych typów
        }
    }
    
    /**
     * Generuje zużycie energii w gospodarstwie dla danego okresu.
     * Używa rzeczywistego zużycia energii z urządzeń pobranych z modułu sterowania.
     * Jeśli nie ma urządzeń, używa wartości domyślnych opartych na okresie dnia.
     * 
     * @param periodNumber numer okresu (1-6)
     * @return zużycie energii w kWh
     */
    private Double generateHouseholdConsumption(Integer periodNumber) {
        // Obliczenie zużycia na podstawie urządzeń z modułu sterowania
        double consumptionFromDevices = calculateHouseholdConsumptionFromDevices(4.0); // okres 4h
        
        // Jeśli mamy urządzenia i zużycie jest znaczące, używamy go
        if (consumptionFromDevices > 0.5) {
            // Dodanie małej losowej zmienności (±5%) dla realizmu
            double variation = 1.0 + (random.nextDouble() - 0.5) * 0.1; // 0.95 - 1.05
            return consumptionFromDevices * variation;
        }
        
        // Fallback: jeśli brak urządzeń lub bardzo małe zużycie, użyj wartości domyślnych
        // Okresy: 1 (0-4h), 2 (4-8h), 3 (8-12h), 4 (12-16h), 5 (16-20h), 6 (20-24h)
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

    /**
     * Sprawdza warunki i generuje alerty jeśli to konieczne.
     * Wykorzystuje moduł alertów do utworzenia i zapisania alertu.
     *
     * @param record rekord symulacji do sprawdzenia
     */
    private void checkAndGenerateAlert(SimulationRecord record) {
        // Integracja z modułem alertów:
        // - My tylko przekazujemy "co to za problem" (anomalyType) + wartość anomalii.
        // - NIE decydujemy o priorytecie ani nie obsługujemy progów (thresholdów) tutaj.
        // - Nie rozbijamy baterii na Low/Full – po prostu "Battery", a wartość sugeruje stan.

        try {
            AlertManager alertManager = new AlertManager();
            DeviceManager deviceManager = new DeviceManager();
            List<Device> devices = deviceManager.listDevices();

            if (devices == null || devices.isEmpty()) {
                return;
            }

            // Wybieramy pierwsze urządzenie jako źródło alertu (jak wcześniej).
            Integer deviceId = devices.get(0).getId();
            java.util.Date date = Timestamp.valueOf(record.getPeriodStart());

            // Alert dot. zużycia z sieci – przekazujemy surową wartość z symulacji.
            if (record.getGridConsumption() != null) {
                Alert gridAlert = alertManager.createAlert(
                        date,
                        record.getGridConsumption().floatValue(),
                        "Grid",
                        deviceId
                );
                if (gridAlert != null) {
                    alertManager.saveAlertToDataBase(gridAlert);
                }
            }

            // Alert dot. baterii – przekazujemy surowy poziom baterii (kWh) z symulacji.
            if (record.getBatteryLevel() != null) {
                Alert batteryAlert = alertManager.createAlert(
                        date,
                        record.getBatteryLevel().floatValue(),
                        "Battery",
                        deviceId
                );
                if (batteryAlert != null) {
                    alertManager.saveAlertToDataBase(batteryAlert);
                }
            }
        } catch (Exception e) {
            // Nie przerywamy symulacji błędami integracji alertów
            System.err.println("Nie udało się wysłać alertu z symulacji: " + e.getMessage());
        }
    }
}
