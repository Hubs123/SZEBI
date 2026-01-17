package pl.szebi.sterowanie.api;

import pl.szebi.sterowanie.DeviceType;

import java.util.List;
import java.util.Map;

public class ControlDtos {

    // ---- Devices ----
    public static class CreateDeviceRequest {
        public String name;
        public DeviceType type;
        public Integer roomId;
    }

    public static class UpdateDeviceStatesRequest {
        public Map<String, Float> states;
    }

    public static class AssignRoomRequest {
        public Integer roomId;
    }

    // ---- Rooms ----
    public static class CreateRoomRequest {
        public String name;
    }

    public static class GroupCommandRequest {
        public DeviceType type;
        public Map<String, Float> states;
    }

    public static class GroupCommandResult {
        public List<Integer> appliedDeviceIds;
        public List<Integer> lockedDeviceIds;
        public List<Integer> missingDeviceIds;
    }

    // ---- Plans ----
    public static class CreatePlanRequest {
        public String name;
    }

    public static class AddRuleRequest {
        public Integer deviceId;
        public Map<String, Float> states;
        public String timeWindow;
    }
}
