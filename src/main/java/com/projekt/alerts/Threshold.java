package com.projekt.alerts;

public class Threshold {
    private final Integer id;
    private String thresholdType;
    private Float valueWarning;
    private Float valueEmergency;

    public Threshold(Integer id, String thresholdType, Float valueWarning, Float valueEmergency) {
        this.id = id;
        this.thresholdType = thresholdType;
        this.valueWarning = valueWarning;
        this.valueEmergency = valueEmergency;
    }

    public Integer getId() {
        return id;
    }

    public String getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(String thresholdType) {
        this.thresholdType = thresholdType;
    }

    public Float getValueWarning() {
        return valueWarning;
    }

    public void setValueWarning(Float valueWarning) {
        this.valueWarning = valueWarning;
    }

    public Float getValueEmergency() {
        return valueEmergency;
    }

    public void setValueEmergency(Float valueEmergency) {
        this.valueEmergency = valueEmergency;
    }

    public void setValues(Float valueWarning, Float valueEmergency) {
        this.valueWarning = valueWarning;
        this.valueEmergency = valueEmergency;
    }
}
