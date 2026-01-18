package pl.szebi.sterowanie;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceManager {
    // automation plan musi mieć dostęp do deviceRepo, stąd static
    static private final RoomRepository roomRepo = new RoomRepository();

    static public final DeviceRepository deviceRepo = new DeviceRepository();

    public Device registerDevice(String name, DeviceType type, Integer roomId) {
        Device d = new Device(name, type, roomId);
        Boolean added = deviceRepo.add(d);
        if (added) {
            return d;
        }
        d.stopTicking();
        return null;
    }

    public Room registerRoom(String name, Integer roomId) {
        Room r = new Room(name);
        Boolean added = roomRepo.add(r);
        if (added) {
            return r;
        }
        return null;
    }

    public Boolean removeDevice(Integer deviceId) {
        if (getDevice(deviceId).isOn())
            return false;
        return deviceRepo.delete(deviceId);
    }

    public Boolean removeRoom(Integer roomId) {
        return roomRepo.delete(roomId);
    }

    public Boolean loadDevicesFromDataBase() {
        return deviceRepo.load();
    }

    public Boolean loadRoomsFromDataBase() {
        return roomRepo.load();
    }

    public Boolean saveDeviceToDatabase(Device device) {
        return deviceRepo.save(device);
    }

    public Boolean saveRoomToDatabase(Room room) {
        return roomRepo.save(room);
    }

    public Map<String, Float> getStates(Integer deviceId) {
        Device d = deviceRepo.findById(deviceId);
        if (d == null) return null;
        return d.getStates();
    }

    public boolean sendCommand(Integer deviceId, Map<String, Float> m) {
        Device d = deviceRepo.findById(deviceId);
        if (d == null || d.isEmergencyLocked()) return false;
        return d.applyCommand(m);
    }

    public List<Device> listDevices() {
        return Collections.unmodifiableList(deviceRepo.findAll());
    }

    public List<Device> listDevicesByType(DeviceType type) {
        return Collections.unmodifiableList(deviceRepo.findByType(type));
    }

    public static Device getDevice(Integer deviceId) {
        return deviceRepo.findById(deviceId);
    }

    public static Room getRoom(Integer roomId) {
        return roomRepo.findById(roomId);
    }

    public List<Room> getRooms() {
        return roomRepo.findAll();
    }

    public List<Device> listRoomDevices(Integer roomId) {
        if (roomId == null) return Collections.emptyList();
        return Collections.unmodifiableList(deviceRepo.findByRoom(roomId));
    }

    public static Boolean applyToRoom(Integer roomId, DeviceType type, Map<String, Float> states) {
        return deviceRepo.applyToRoom(roomId, type, states);
    }

    public static Boolean applyCommands(List<Pair<Integer, Map<String, Float>>> devicesStates) {
        return applyCommands(devicesStates, false);
    }

    public static Boolean applyCommands(List<Pair<Integer, Map<String, Float>>> devicesStates, boolean force) {
        return deviceRepo.applyCommands(devicesStates, force);
    }
}
