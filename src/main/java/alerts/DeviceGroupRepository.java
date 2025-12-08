package alerts;

import java.util.List;

public class DeviceGroupRepository {
    private List<DeviceGroup> groups;

    public DeviceGroupRepository() {
    }

    public List<DeviceGroup> getAllGroups() {
        return groups;
    }

    public DeviceGroup getGroupById(int id) {
        for (DeviceGroup g : groups) {
            if (g.getDeviceGroupId() == id) {
                return g;
            }
        }
        return null;
    }

    public Boolean addGroup(DeviceGroup group) {
        try {
            groups.add(group);}
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public Boolean deleteGroup(int id) {
        for (DeviceGroup g : groups) {
            if (g.getDeviceGroupId() == id) {
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