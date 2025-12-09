package com.projekt.alerts;

import java.util.Date;
import java.util.List;

public class Alert {
    private Integer id;
    private final Date alertDate;
    private final Float anomalyValue;
    private final String anomalyType;
    private final DeviceGroup deviceGroup;
    private final Integer deviceId;
    private Priority priority;

    public Alert(Date alertDate, Float anomalyValue, String anomalyType, DeviceGroup deviceGroup,Integer deviceId) {
        this.id = null;
        this.alertDate = alertDate;
        this.anomalyValue = anomalyValue;
        this.anomalyType = anomalyType;
        this.deviceGroup = deviceGroup;
        this.deviceId = deviceId;
        this.priority = Priority.Information;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getAlertDate() {
        return alertDate;
    }

    public DeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    public Float getAnomalyValue() {
        return anomalyValue;
    }

    public String getAnomalyType() {
        return anomalyType;
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
        DeviceGroup dg = getDeviceGroup();
        List<Threshold> thresholds = dg.getThresholds();
        for (Threshold threshold : thresholds) {
            if (threshold.getThresholdType().equals(getAnomalyType())) {
                if(threshold.getValueWarning()>threshold.getValueEmergency()) {
                    if (this.anomalyValue <= threshold.getValueEmergency()) {
                        setPriority(Priority.Emergency);
                    } else if (this.anomalyValue <= threshold.getValueWarning()) {
                        setPriority(Priority.Warning);
                    } // TO DO Add exception
                }
                else if(threshold.getValueWarning()<threshold.getValueEmergency()) {
                    if (this.anomalyValue > threshold.getValueWarning()) {
                        setPriority(Priority.Warning);
                    } else if (this.anomalyValue > threshold.getValueEmergency()) {
                        setPriority(Priority.Emergency);
                    } // TO DO Add exception
                }
                break;
            }
        }
    }

    public String createMessage() {
        return "New alert with id: " + getId().toString() + "\nalerty.Priority level: " +
                getPriority().toString() + "\nReported at: " + getAlertDate().toString() +
                "\nComing from the device with id:" + getDeviceId().toString() +
                "\nThe value of the anomaly equals:" + getAnomalyValue().toString();
    }

    public void checkAutomaticReaction() {
        List<AutomaticReaction> reactions = deviceGroup.getReactions();
        for (AutomaticReaction reaction : reactions) {
            if (reaction.)

        }
        
    }
}
