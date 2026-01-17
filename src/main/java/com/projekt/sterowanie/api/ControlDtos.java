package com.projekt.sterowanie.api;

import com.projekt.sterowanie.DeviceType;

import java.util.List;
import java.util.Map;

public class ControlDtos {

    // ---- Devices ----
    public static class CreateDeviceRequest {
        public String name;
        public DeviceType type;
        public Integer roomId; // opcjonalne
    }

    public static class UpdateDeviceStatesRequest {
        public Map<String, Float> states;
    }

    public static class AssignRoomRequest {
        public Integer roomId; // może być null -> "odpięcie"
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
        public List<Integer> lockedDeviceIds;  // niezmienione (np. blokada alarmowa/emergency)
        public List<Integer> missingDeviceIds; // jeśli coś zniknęło w trakcie
    }

    // ---- Plans ----
    public static class CreatePlanRequest {
        public String name;
    }

    public static class AddRuleRequest {
        public Integer deviceId;
        public Map<String, Float> states;
        public String timeWindow; // opcjonalne (jeśli Twoje AutomationRule to wspiera)
    }
}
