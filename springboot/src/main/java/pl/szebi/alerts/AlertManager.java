package pl.szebi.alerts;

import pl.szebi.sterowanie.Device;
import pl.szebi.sterowanie.DeviceManager;
import pl.szebi.sterowanie.DeviceType;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AlertManager {
    static public final AlertRepository alertRepo = new AlertRepository();

    public Alert createAlert(Date date, Float anomalyValue, String anomalyType, Integer deviceId) {
        DeviceGroup deviceGroup = null;
        DeviceType type = DeviceManager.getDevice(deviceId).getType();
        switch (type) {
            case noSimulation:
                deviceGroup = new DeviceGroup(1, "light", null, null);
                break;
            case thermometer:
                deviceGroup = new DeviceGroup(2, "thermometer", null, null);
                break;
            case smokeDetector:
                deviceGroup = new DeviceGroup(3, "smokeDetector", null, null);
        }
        Alert a = new Alert(date, anomalyValue, anomalyType, deviceGroup, deviceId);
        try {
            a.findPriorityLevel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(a.getPriority() == Priority.Emergency) {
            try {
                a.checkAutomaticReaction();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Boolean added = alertRepo.add(a);
        if (added) {
            return a;
        }
        return null;
    }

    public Boolean removeAlert(Integer alertId) {
        return alertRepo.delete(alertId);
    }

    public Boolean saveAlertToDataBase(Alert alert) {
        return alertRepo.saveToDB(alert);
    }

    public List<Alert> listAlerts() {
        return Collections.unmodifiableList(alertRepo.getAll());
    }
}
