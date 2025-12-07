package com.projekt.sterowanie;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Device {
    private Integer id;
    private String name;
    private DeviceType type;
    private Integer roomId = null;
    private Map<String, Float> states = new HashMap<>();

    public Device(Integer id, String name, DeviceType type, Integer roomId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.roomId = roomId;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeviceType getType() {
        return type;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Map<String, Float> getStates() {
        return Collections.unmodifiableMap(new HashMap<>(states));
    }

    public Boolean applyCommand(Map<String, Float> m) {
        boolean result = true;
        for (Map.Entry<String, Float> entry : m.entrySet()) {
            String key = entry.getKey();
            Float value = entry.getValue();
            try { states.replace(key, value); }
            catch (Exception e) { result = false; }
        }
        return result;
    }

    public void tick() {
        // TODO: zrobić tak, żeby enum DeviceType zawierał w sobie odpowiednie implementacje SimulationModel
        try {
            String className = "com.projekt.sterowanie." + type.name();
            Class<?> simModel = Class.forName(className);
            SimulationModel instance = (SimulationModel) simModel.getDeclaredConstructor().newInstance();
            instance.tick(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
