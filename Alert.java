import java.util.Date;

public class Alert {
    private final Integer alertId;
    private final Date alertDate;
    private final Device device;
    private final Float deviceValue;
    private final String alertThresholdType;
    private Priority priority;

    public Alert(Integer alertId, Date alertDate, Device device, Float deviceValue, String alertThresholdType, Priority priority) {
        this.alertId = alertId;
        this.alertDate = alertDate;
        this.device = device;
        this.deviceValue = deviceValue;
        this.alertThresholdType = alertThresholdType;
        this.priority = priority;
    }

    public Integer getAlertId() {
        return alertId;
    }

    public Date getAlertDate() {
        return alertDate;
    }

    public Device getDevice() {
        return device;
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

//    DO ZROBIENIA
//    public void setPriority(Priority priority) {
//        this.priority = priority;
//    }

//    public String createMessage() {
//
//    }
}
