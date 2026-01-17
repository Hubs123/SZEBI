package pl.szebi.sterowanie;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Room {
    private Integer id;
    private String name;
    private Set<Integer> deviceIds = new HashSet<>();

    public Room(String name) {
        this.id = null;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }
    void setId(Integer id) { this.id = id; }

    public String getName() {
        return name;
    }

    public Set<Integer> listDeviceIds() {
        return Collections.unmodifiableSet(deviceIds);
    }

    public Boolean assignDevice(Integer deviceId) {
        return deviceIds.add(deviceId);
    }

    public Boolean unassignDevice(Integer deviceId) {
        return deviceIds.remove(deviceId);
    }
}
