package com.projekt.alerts;

import java.util.List;

public class DeviceGroupRepository {
    private List<DeviceGroup> groups;

    public List<DeviceGroup> getAll() {
        return groups;
    }

    public DeviceGroup getById(int id) {
        for (DeviceGroup g : groups) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }

    public Boolean add(DeviceGroup group) {
        try {
            groups.add(group);}
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean delete(int id) {
        for (DeviceGroup g : groups) {
            if (g.getId() == id) {
                try {
                    groups.remove(g);}
                catch (Exception e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}