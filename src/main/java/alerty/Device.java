package alerty;

public class Device {
    private final Integer deviceId;
    private final Integer deviceGroupId;
    private String deviceName;

    public Device(Integer deviceId, Integer deviceGroupId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceGroupId = deviceGroupId;
        this.deviceName = deviceName;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public Integer getDeviceGroupId() {
        return deviceGroupId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
