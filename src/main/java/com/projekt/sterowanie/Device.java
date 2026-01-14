package com.projekt.sterowanie;

import com.projekt.time.TimeControl;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

public class Device {
    private Integer id;
    private String name;
    private Integer deviceGroupId;
    private DeviceType type;
    private Integer roomId;
    private final Map<String, Float> states = new HashMap<>();
    private final SimulationModel model;
    private volatile Thread tickThread;

    public Device(String name, DeviceType type, Integer deviceGroupId, Integer roomId) {
        this.id = null;
        this.name = name;
        this.deviceGroupId = deviceGroupId;
        this.type = type;
        this.roomId = roomId;
        synchronized (states) {
            states.put("power", 1.0f);
            switch (type) {
                case thermometer:
                    states.put("temp", 21.0f);
                    break;
                case smokeDetector:
                    states.put("smokeDetected", 0.0f);
                    break;
            }
        }
        this.model = type.newModelInstance();
    }

    public Integer getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public Integer getDeviceGroupId() {
        return deviceGroupId;
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
        synchronized (states) {
            return Map.copyOf(states);
        }
    }

    public float getState(String key) {
        synchronized (states) {
            return states.get(key);
        }
    }

    public boolean isOn() {
        return getState("power") == 1.0f;
    }

    public Boolean applyCommand(Map<String, Float> m) {
        boolean result = true;
        synchronized (states) {
            for (Map.Entry<String, Float> entry : m.entrySet()) {
                String key = entry.getKey();
                Float value = entry.getValue();
                // replace zwraca null jeśli klucza nie było
                Float prev = states.replace(key, value);
                if (prev == null) result = false;
            }
        }
        return result;
    }

    public void tick() {
        model.tick(this);
    }

    public void startTicking() {
        Thread existing = tickThread;
        if (existing != null && existing.isAlive()) return;

        Thread t = new Thread(this::tickLoop, "tick-" + Integer.toHexString(System.identityHashCode(this)));
        t.setDaemon(true);
        tickThread = t;
        t.start();
    }

    public void stopTicking() {
        Thread t = tickThread;
        if (t != null) t.interrupt();
    }

    private void tickLoop() {
        Instant next = TimeControl.now();
        while (!Thread.currentThread().isInterrupted()) {
            if (!isOn()) {
                LockSupport.parkNanos(1_000_000L); // ~1ms
                next = TimeControl.now();
                continue;
            }
            Instant now = TimeControl.now();
            if (now.isBefore(next)) {
                long nanos = Duration.between(now, next).toNanos();
                LockSupport.parkNanos(Math.min(nanos, 2_000_000L));
                continue;
            }
            tick();
            next = now.plusMillis(1);
        }
    }
}
