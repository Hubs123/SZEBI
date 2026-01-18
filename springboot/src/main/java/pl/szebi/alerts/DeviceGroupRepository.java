package pl.szebi.alerts;

import java.util.ArrayList;
import java.util.List;

public class DeviceGroupRepository {
    private static List<DeviceGroup> groups = new ArrayList<>();

    static {
        groups.add(new DeviceGroup(1, "Urządzenia Niesymulowane", null, null));
        groups.add(new DeviceGroup(2, "Termometry", null, null));
        groups.add(new DeviceGroup(3, "Czujniki Dymu", null, null));
        groups.add(new DeviceGroup(4, "Oświetlenie", null, null));
    }

    public List<DeviceGroup> getAll() {
        return groups;
    }

    public static DeviceGroup getById(int id) {
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