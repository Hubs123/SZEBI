import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceRepository {
    private List<Device> devices = new ArrayList<>();

    public Boolean save(Device device) {
        //TODO: zapis do bazy
        return null;
    }

    public Boolean add(Device device) {
        if (device == null) return false;
        if (device.getId() == null) return false;
        return devices.add(device);
    }

    public Boolean delete(Integer deviceId) {
        if (devices.isEmpty()) return false;
        return devices.removeIf(device -> device.getId() != null && device.getId().equals(deviceId));
    }

    public Device findById(Integer deviceId) {
        for (Device d : devices) {
            if (d.getId() != null && d.getId().equals(deviceId)) {
                return d;
            }
        }
        return null;
    }

    public List<Device> findByRoom(Integer roomId) {
        if (roomId == null) return Collections.emptyList();

        List<Device> result = new ArrayList<>();
        for (Device d : devices) {
            if (roomId.equals(d.getRoomId())) {
                result.add(d);
            }
        }
        return result;
    }

    public List<Device> findAll() {
        return new ArrayList<>(devices);
    }

    public List<Device> findByType(DeviceType type) {
        if (type == null) return Collections.emptyList();

        List<Device> result = new ArrayList<>();
        for (Device d : devices) {
            if (type.equals(d.getType())) {
                result.add(d);
            }
        }
        return result;
    }
}
