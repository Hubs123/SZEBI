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

    public static Boolean applyToRoom(Integer roomId, DeviceType type, Map<String, Float> states) {
        return deviceRepo.applyToRoom(roomId, type, states);
    }

    public static Boolean applyCommands(List<Pair<Integer, Map<String, Float> > > devicesStates) {
        return deviceRepo.applyCommands(devicesStates);
    }
}
