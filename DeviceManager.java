import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeviceManager {
    private DeviceRepository deviceRepo;

    public DeviceManager(DeviceRepository deviceRepo) {
        this.deviceRepo = deviceRepo;
    }

    //TODO: chyba dodać tu też metode do zapisu do bazy wykorzystującą save() z repo

    public Integer registerDevice(Integer id, String name, DeviceType type, Integer roomId) {
        Device d = new Device(id, name, type, roomId);
        Boolean added = deviceRepo.add(d);
        return added ? d.getId() : null;
    }

    public Boolean removeDevice(Integer deviceId) {
        return deviceRepo.delete(deviceId);
    }

    public Map<String, Float> getStates(Integer deviceId) {
        Device d = deviceRepo.findById(deviceId);
        if (d == null) return null;
        return d.getStates();
    }

    public boolean sendCommand(Integer deviceId, Map<String, Float> m) {
        //TODO: dostosować po zrobieniu applyCommand()
        Device d = deviceRepo.findById(deviceId);
        if (d == null) return false;
        Boolean changed = d.applyCommand(m);
        return changed;
    }

    public List<Device> listDevices() {
        return Collections.unmodifiableList(deviceRepo.findAll());
    }

    public List<Device> listRoomDevices(Integer roomId) {
        if (roomId == null) return Collections.emptyList();
        return Collections.unmodifiableList(deviceRepo.findByRoom(roomId));
    }
}
