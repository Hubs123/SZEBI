package com.projekt.alerts;

import com.projekt.sterowanie.DeviceManager;
import com.projekt.sterowanie.DeviceType;

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
                deviceGroup = DeviceGroupRepository.getById(1);
                break;
            case thermometer:
                deviceGroup = DeviceGroupRepository.getById(2);
                break;
            case smokeDetector:
                deviceGroup = DeviceGroupRepository.getById(3);
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
