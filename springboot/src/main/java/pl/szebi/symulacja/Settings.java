package pl.szebi.symulacja;

/**
 * Klasa przechowująca parametry konfiguracyjne symulacji.
 * Zawiera ustawienia dotyczące mocy paneli fotowoltaicznych
 * oraz pojemności magazynu energii.
 */
public class Settings {
    private Double panelPower; // moc paneli PV w kW
    private Double batteryCapacity; // pojemność akumulatora w kWh

    public Settings() {
        // wartości domyślne
        this.panelPower = 5.0; // 5 kW
        this.batteryCapacity = 10.0; // 10 kWh
    }

    public Settings(Double panelPower, Double batteryCapacity) {
        if (panelPower != null && panelPower <= 0) {
            throw new IllegalArgumentException("Moc paneli musi być większa od zera");
        }
        if (batteryCapacity != null && batteryCapacity <= 0) {
            throw new IllegalArgumentException("Pojemność magazynu musi być większa od zera");
        }
        this.panelPower = panelPower;
        this.batteryCapacity = batteryCapacity;
    }

    public Double getPanelPower() {
        return panelPower;
    }

    public void setPanelPower(Double panelPower) {
        if (panelPower != null && panelPower <= 0) {
            throw new IllegalArgumentException("Moc paneli musi być większa od zera");
        }
        this.panelPower = panelPower;
    }

    public Double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Double batteryCapacity) {
        if (batteryCapacity != null && batteryCapacity <= 0) {
            throw new IllegalArgumentException("Pojemność magazynu musi być większa od zera");
        }
        this.batteryCapacity = batteryCapacity;
    }

    public void validate() {
        if (panelPower == null || panelPower <= 0) {
            throw new IllegalStateException("Moc paneli nie jest ustawiona lub jest nieprawidłowa");
        }
        if (batteryCapacity == null || batteryCapacity <= 0) {
            throw new IllegalStateException("Pojemność magazynu nie jest ustawiona lub jest nieprawidłowa");
        }
    }
}
