package com.projekt.alerts;

import java.util.Date;
import java.util.List;
import com.projekt.sterowanie.Device;

public class Alert {
    private final Integer alertId;
    private final Date alertDate;
    private final Float deviceValue;
    private final String alertThresholdType;
    private final DeviceGroup deviceGroup;
    private final Integer deviceId;
    private Priority priority=null;

    public Alert(Integer alertId, Date alertDate, Float deviceValue, String alertThresholdType, DeviceGroup deviceGroup,Integer deviceId) {
        this.alertId = alertId;
        this.alertDate = alertDate;
        this.deviceValue = deviceValue;
        this.alertThresholdType = alertThresholdType;
        this.deviceGroup = deviceGroup;
        this.deviceId = deviceId;
        findPriorityLevel();
    }

    public Integer getAlertId() {
        return alertId;
    }

    public Date getAlertDate() {
        return alertDate;
    }

    public Device getDevice() {
        for (Device device : deviceGroup.getDevices()) {
            if (deviceId.equals(device.getId())) {
                return device;
            }
        }
        return null; // TO DO Add exception
    }

    public DeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    public Float getDeviceValue() {
        return deviceValue;
    }

    public String getAlertThresholdType() {
        return alertThresholdType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void findPriorityLevel() {
        Float anomalyValue = getDeviceValue();
        DeviceGroup dg = getDeviceGroup();
        List<Threshold> thresholds = dg.getThresholds();
        for (Threshold threshold : thresholds) {
            if (threshold.getThresholdType().equals(getAlertThresholdType())) {
                if (anomalyValue<=threshold.getValueInfo()) {
                    setPriority(Priority.Information);
                }
                else if (anomalyValue<=threshold.getValueWarning()) {
                    setPriority(Priority.Warning);
                }
                else if (anomalyValue<=threshold.getValueEmergency()) {
                    setPriority(Priority.Emergency);
                } // TO DO Add exception
                break;
            }
        }
    }

    public String createMessage() {
        return "New alert with id: " + getAlertId().toString() + "\nalerty.Priority level: " +
                getPriority().toString() + "\nReported at: " + getAlertDate().toString() +
                "\nComing from the device with id:" + getDeviceId().toString() +
                "\nThe value of the anomaly equals:" + getDeviceValue().toString();
    }
}
