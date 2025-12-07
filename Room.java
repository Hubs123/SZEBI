import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Room {
    private Integer id;
    private String name;
    private Set<Integer> deviceIds = new HashSet<>();

    public Room(Integer id, String name) throws SQLException {
        this.id = id;
        this.name = name;
    }

    public Set<Integer> listDevices() {
//        List<Device> devices = new ArrayList<>();
//        PreparedStatement ps = Db.conn.prepareStatement(
//                "select * from devices where room_id = ?");
//        ps.setInt(1, id);
//        ResultSet rs = ps.executeQuery();
//        while (rs.next()) {
//            int deviceId = rs.getInt("id");
//            String deviceName = rs.getString("name");
//            DeviceType deviceType = DeviceType.valueOf(rs.getString("type"));
//            int roomId = rs.getInt("room_id");
//            Device device = new Device(deviceId, deviceName, deviceType, roomId);
//            devices.add(device);
//        }
//        return devices;
        return Collections.unmodifiableSet(deviceIds);
    }

    public Boolean assignDevice(Integer deviceId) {
        return deviceIds.add(deviceId);
    }

    public Boolean unassignDevice(Integer deviceId) {
        return deviceIds.remove(deviceId);
    }

    // to musi być w room manager
//    public Boolean turnOnAllDevices() {
//        List<Device> devices = listDevices();
//        boolean result = true;
//        for (Device device : devices) {
//            if (!device.applyCommand(Map.of("power", 1.0f))) {
//                result = false;
//                // bez break - niech włączy ile się da
//            }
//        }
//        return result;
        // jeśli false - błąd typu "nie udało się włączyć niektórych urządzeń"
//    }

//    public Boolean turnOffAllDevices() {
//        List<Device> devices = listDevices();
//        boolean result = true;
//        for (Device device : devices) {
//            if (!device.applyCommand(Map.of("power", 0.0f))) {
//                result = false;
//            }
//        }
//        return result;
//    }

//    public Boolean applyToRoom(DeviceType type, Map<String, Float> states) {
//        List<Device> devices = listDevices();
//        boolean result = true;
//        for (Device device : devices) {
//            if (!device.applyCommand(states)) {
//                result = false;
//            }
//        }
//        return result;
//    }
}
