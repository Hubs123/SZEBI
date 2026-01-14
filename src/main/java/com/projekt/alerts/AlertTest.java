package com.projekt.alerts;

import com.projekt.sterowanie.Device;
import com.projekt.sterowanie.DeviceManager;
import com.projekt.sterowanie.DeviceType;

import java.util.Date;

public class AlertTest {
    public static void main(String[] args) {
        DeviceManager manager = new DeviceManager();
        // domyślnie żarówka (typ noSimulation) jest włączona - power -> 1.0
        Device device = manager.registerDevice("zarowka2", DeviceType.noSimulation, 1, null);
        if (!manager.saveDeviceToDatabase(device)) {
            System.out.println("Could not save device to database");
        }
        Integer deviceId = device.getId();
        if (deviceId == null) {
            System.out.println("The device id is null");
        }

        AlertManager alertManager = new AlertManager();
        DeviceGroup deviceGroup = new DeviceGroup(1, "light", null, null);
        Threshold threshold = new Threshold(1, "battery", 20F, 5F);
        AutomaticReaction reaction = new AutomaticReaction(1, "turnOff");
        if(deviceGroup.addReaction(reaction)) {
            System.out.println("Reaction added");
        } else {
            System.out.println("Reaction not added");
        }
        threshold.setReactionId(reaction.getId());
        if(deviceGroup.addThreshold(threshold)) {
            System.out.println("Threshold added");
        } else {System.out.println("Threshold not added");}
        Date today = new Date();
        Alert alert = alertManager.createAlert(today, 4F, "battery", deviceGroup, device.getId());
        if (manager.getStates(deviceId).get("power") == 0.0f) {
            System.out.println("It slayed");
        }
        else {
            System.out.println("It flopped");
        }

        if(alertManager.saveAlertToDataBase(alert)) {
            System.out.println("Alert saved");
        } else {
            System.out.println("Alert not saved");
        }
    }
}