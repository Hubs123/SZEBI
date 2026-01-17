package pl.szebi.sterowanie;

import pl.szebi.time.TimeControl;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;

public class ThermometerSimulation implements SimulationModel {

    private final double minTemp = 18.0;
    private final double meanTemp = 21.0;
    private final double maxTemp = 24.0;
    private final double amplitude = 2.8;
    private final double tauSec = 5 * 60; // 5 min

    private final Random rng = new Random();

    private Instant last;
    private double temp;
    private boolean initialized = false;
    private double noise = 0.0;

    private double dailyTargetTemp(Instant now) {
        ZoneId zone = TimeControl.getClock().getZone();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, zone);
        double seconds = zdt.toLocalTime().toSecondOfDay() + zdt.getNano() / 1e9;
        double hours = seconds / 3600.0;
        double sin = Math.sin(2.0 * Math.PI * (hours - 10.0) / 24.0); // max około 16:00
        return meanTemp + amplitude * sin;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public void tick(Device device) {
        Instant now = TimeControl.now();
        if (!initialized) {
            temp = device.getState("temp");
            temp = clamp(temp, minTemp, maxTemp);
            last = now;
            initialized = true;
            return;
        }
        double dt = Duration.between(last, now).toNanos() / 1e9;

        if (dt <= 0) dt = 0.001;
        if (dt > 1.0) dt = 1.0;

        double target = dailyTargetTemp(now) + noise;

        noise += rng.nextGaussian() * 0.0008;
        noise *= 0.9995;
        noise = clamp(noise, -0.25, 0.25);

        double alpha = 1.0 - Math.exp(-dt / tauSec);
        temp += (target - temp) * alpha;

        temp = clamp(temp, minTemp, maxTemp);

        // funkcje biblioteczne w tej klasie używają double
        // więc wygodniej konwersja na float pod sam koniec
        device.applyCommand(Map.of("temp", (float)temp));
        last = now;
    }
}
