package com.projekt.symulacja;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Klasa reprezentująca rekord symulacji dla jednego okresu czasowego (4h).
 * Zawiera wszystkie dane wygenerowane podczas symulacji.
 */
public class SimulationRecord {
    private Integer id;
    private LocalDate simulationDate;
    private Integer periodNumber; // numer okresu (1-6)
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Parametry wejściowe okresu
    private Double sunlightIntensity; // nasłonecznienie (0.0 - 1.0)
    
    // Wyniki obliczeń
    private Double pvProduction; // energia wyprodukowana przez panele PV (kWh)
    private Double energyStored; // energia zmagazynowana w akumulatorze (kWh)
    private Double batteryLevel; // poziom naładowania akumulatora po okresie (kWh)
    private Double gridConsumption; // energia pobrana z sieci (kWh)
    private Double gridFeedIn; // energia oddana do sieci (kWh)
    
    // Parametry systemu użyte w symulacji
    private Double panelPower; // moc paneli (kW)
    private Double batteryCapacity; // pojemność magazynu (kWh)

    public SimulationRecord() {
    }

    public SimulationRecord(LocalDate simulationDate, Integer periodNumber, 
                           LocalDateTime periodStart, LocalDateTime periodEnd,
                           Double sunlightIntensity,
                           Double pvProduction, Double energyStored,
                           Double batteryLevel, Double gridConsumption, Double gridFeedIn,
                           Double panelPower, Double batteryCapacity) {
        this.simulationDate = simulationDate;
        this.periodNumber = periodNumber;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.sunlightIntensity = sunlightIntensity;
        this.pvProduction = pvProduction;
        this.energyStored = energyStored;
        this.batteryLevel = batteryLevel;
        this.gridConsumption = gridConsumption;
        this.gridFeedIn = gridFeedIn;
        this.panelPower = panelPower;
        this.batteryCapacity = batteryCapacity;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getSimulationDate() {
        return simulationDate;
    }

    public void setSimulationDate(LocalDate simulationDate) {
        this.simulationDate = simulationDate;
    }

    public Integer getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(Integer periodNumber) {
        this.periodNumber = periodNumber;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Double getSunlightIntensity() {
        return sunlightIntensity;
    }

    public void setSunlightIntensity(Double sunlightIntensity) {
        this.sunlightIntensity = sunlightIntensity;
    }

    public Double getPvProduction() {
        return pvProduction;
    }

    public void setPvProduction(Double pvProduction) {
        this.pvProduction = pvProduction;
    }

    public Double getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(Double energyStored) {
        this.energyStored = energyStored;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Double getPanelPower() {
        return panelPower;
    }

    public void setPanelPower(Double panelPower) {
        this.panelPower = panelPower;
    }

    public Double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public Double getGridConsumption() {
        return gridConsumption;
    }

    public void setGridConsumption(Double gridConsumption) {
        this.gridConsumption = gridConsumption;
    }

    public Double getGridFeedIn() {
        return gridFeedIn;
    }

    public void setGridFeedIn(Double gridFeedIn) {
        this.gridFeedIn = gridFeedIn;
    }
}
