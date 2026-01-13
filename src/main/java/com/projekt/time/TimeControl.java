package com.projekt.time;

import java.time.Clock;
import java.time.Instant;

public class TimeControl {
    private static volatile Clock clock = Clock.systemDefaultZone();

    private TimeControl() {}

    public static Instant now() {
        return Instant.now(clock);
    }

    public static Clock getClock() {
        return clock;
    }
}
