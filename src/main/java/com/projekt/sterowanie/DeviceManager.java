package com.projekt.sterowanie;

import com.projekt.alerts.DeviceGroupRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceManager {
    // automation plan musi mieć dostęp do deviceRepo, stąd static
    private final RoomRepository roomRepo = new RoomRepository();

    static public final DeviceRepository deviceRepo = new DeviceRepository();
    static public final DeviceGroupRepository groupRepo = new DeviceGroupRepository();

    public Device registerDevice(String name, DeviceType type, Integer deviceGroupId, Integer roomId) {
        Device d = new Device(name, type, deviceGroupId, roomId);
        Boolean added = deviceRepo.add(d);
        if (added) {
            return d;
        }
        d.stopTicking();
        return null;
    }

    public Boolean removeDevice(Integer deviceId) {
        return deviceRepo.delete(deviceId);
    }

    public Boolean saveDeviceToDatabase(Device device) {
        return deviceRepo.save(device);
    }

    // warto zastanowić się nad zmianą nazwy DeviceManager na np DeviceRoomManager
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
        if (d == null) return false;
        return d.applyCommand(m);
    }

    public List<Device> listDevices() {
        return Collections.unmodifiableList(deviceRepo.findAll());
    }

    public List<Device> listRoomDevices(Integer roomId) {
        if (roomId == null) return Collections.emptyList();
        return Collections.unmodifiableList(deviceRepo.findByRoom(roomId));
    }

    public Boolean turnOnAllDevicesInRoom(Integer roomId) {
        List<Device> devices = deviceRepo.findByRoom(roomId);
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

    public Boolean turnOffAllDevicesInRoom(Integer roomId) {
        List<Device> devices = deviceRepo.findByRoom(roomId);
        boolean result = true;
        for (Device device : devices) {
            if (!device.applyCommand(Map.of("power", 0.0f))) {
                result = false;
            }
        }
        return result;
    }

    public Boolean applyToRoom(Integer roomId, DeviceType type, Map<String, Float> states) {
        List<Device> devices = deviceRepo.findByRoom(roomId);
        boolean result = true;
        for (Device device : devices) {
            if (!device.applyCommand(states)) {
                result = false;
            }
        }
        return result;
    }
}
