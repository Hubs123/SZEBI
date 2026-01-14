package com.projekt.sterowanie;

import com.projekt.time.TimeControl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;

public class SmokeDetectorSimulation implements SimulationModel {

    private final Random rng = new Random();

    private Instant smokeUntil = Instant.EPOCH;

    private long sampleExpMs(Random rng) {
        double u = 1.0 - rng.nextDouble();
        double delay = -Math.log(u) * Duration.ofDays(30).toMillis();
        return (long) Math.max(1.0, delay);
    }

    private Instant nextSmoke = TimeControl.now().plusMillis(sampleExpMs(rng));

    @Override
    public void tick(Device device) {
        Instant now = TimeControl.now();
        if (now.isBefore(smokeUntil)) {
            device.applyCommand(Map.of("smokeDetected", 1.0f));
            return;
        }
        if (device.getState("smokeDetected") == 1.0f) {
            device.applyCommand(Map.of("smokeDetected", 0.0f));
            nextSmoke = now.plusMillis(sampleExpMs(rng));
        }
        if (!now.isBefore(nextSmoke)) {
            device.applyCommand(Map.of("smokeDetected", 1.0f));
            smokeUntil = now.plusMillis(10_000);
        }
    }
}
