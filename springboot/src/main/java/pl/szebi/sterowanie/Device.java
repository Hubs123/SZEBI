package pl.szebi.sterowanie;

import pl.szebi.time.TimeControl;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

public class Device {
    private volatile Integer id;
    private String name;
    private DeviceType type;
    private volatile Integer roomId;
    private final Map<String, Float> states = new HashMap<>();
    private final SimulationModel model;
    private volatile Thread tickThread;
    private volatile Instant emergencyLockUntil = Instant.EPOCH;

    public Device(String name, DeviceType type, Integer roomId) {
        this.id = null;
        this.name = name;
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
        startTicking();
    }

    public Integer getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
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
            return states.getOrDefault(key, 0.0f);
        }
    }

    public boolean isOn() {
        return getState("power") == 1.0f;
    }

    public void emergencyLock() {
        emergencyLockUntil = TimeControl.now().plus(Duration.ofMinutes(1));
    }

    public boolean isEmergencyLocked() {
        Instant now = TimeControl.now();
        return now.isBefore(emergencyLockUntil);
    }

    public Boolean applyCommand(Map<String, Float> m) {
        return applyCommand(m, false);
    }

    public Boolean applyCommand(Map<String, Float> m, boolean force) {
        if (!force && isEmergencyLocked()) return false;
        boolean result = true;
        synchronized (states) {
            for (Map.Entry<String, Float> entry : m.entrySet()) {
                String key = entry.getKey();
                Float value = entry.getValue();
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
