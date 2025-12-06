import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Room {
    private Integer id;
    private String name;

    public Room(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<Device> listDevices() {
        List<Device> devices = new ArrayList<>();
        // devices = select * from devices where room_id = id
        return devices;
    }

    public boolean assignDevice(int deviceId) {
        Device device = new Device(1, "a", DeviceType.BOMBA, 1);
        // device = select 1 from devices where device_id = deviceId
        device.setRoomId(id);
        return device.getRoomId() == id;
    }

    public boolean unassignDevice(int deviceId) {
        Device device = new Device(1, "a", DeviceType.BOMBA, 1);
        // device = select 1 from devices where device_id = deviceId
        device.setRoomId(-1); // załóżmy że tak wygląda brak pokoju
        return device.getRoomId() == -1;
    }

    public boolean turnOnAllDevices() {
        List<Device> devices = listDevices();
        boolean result = true;
        for (Device device : devices) {
            if (!device.applyCommand(Map.of("power", 1.0f))) {
                result = false;
                // bez break - niech włączy ile się da
            }
        }
        return result;
        // jeśli false - błąd typu "nie udało się włączyć niektórych urządzeń"
    }

    public boolean turnOffAllDevices() {
        List<Device> devices = listDevices();
        boolean result = true;
        for (Device device : devices) {
            if (!device.applyCommand(Map.of("power", 0.0f))) {
                result = false;
            }
        }
        return result;
    }

    public boolean applyToRoom(DeviceType type, Map<String, Float> states) {
        List<Device> devices = listDevices();
        boolean result = true;
        for (Device device : devices) {
            if (!device.applyCommand(states)) {
                result = false;
            }
        }
        return result;
    }
}
