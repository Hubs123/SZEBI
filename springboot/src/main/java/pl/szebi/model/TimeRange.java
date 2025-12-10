package pl.szebi.model;

import java.time.LocalDateTime;

public class TimeRange {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeRange() {
    }

    public TimeRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public boolean isValid() {
        return start != null && end != null && start.isBefore(end);
    }

    public boolean contains(LocalDateTime timestamp) {
        return timestamp != null && !timestamp.isBefore(start) && !timestamp.isAfter(end);
    }
}

