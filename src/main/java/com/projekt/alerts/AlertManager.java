package com.projekt.alerts;

import com.projekt.sterowanie.Device;
import com.projekt.sterowanie.DeviceManager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AlertManager {
    static public final AlertRepository alertRepo = new AlertRepository();

    public Alert createAlert(Date alertDate, Float anomalyValue, String anomalyType, Integer deviceId) {
        DeviceGroup deviceGroup = findDeviceGroup(deviceId);
        Alert a = new Alert(alertDate, anomalyValue, anomalyType, deviceGroup, deviceId);
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

    private DeviceGroup findDeviceGroup(Integer deviceId) {
        DeviceGroup deviceGroup = null;
        Device device = DeviceManager.getDevice(deviceId);
        switch (device.getType()) {
            case noSimulation:
                deviceGroup = DeviceGroupRepository.getById(1);
                break;
            case thermometer:
                deviceGroup = DeviceGroupRepository.getById(2);
                break;
            case smokeDetector:
                deviceGroup = DeviceGroupRepository.getById(3);
        }
        assert deviceGroup != null;
        if (deviceGroup.getDeviceById(deviceId) == null) {
            deviceGroup.addDevice(device);
        }
        return deviceGroup;
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
