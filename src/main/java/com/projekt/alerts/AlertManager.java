package com.projekt.alerts;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AlertManager {
    static public AlertRepository alertRepo = new AlertRepository();

    public Alert createAlert(Date date, Float anomalyValue, String anomalyType, DeviceGroup deviceGroup, Integer deviceId) {
        Alert a = new Alert(date, anomalyValue, anomalyType, deviceGroup, deviceId);
        try {
            a.findPriorityLevel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(a.getPriority() == Priority.Emergency) {
            // executeReaction
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
