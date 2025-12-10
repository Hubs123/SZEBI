package pl.szebi.repository;

import pl.szebi.model.Measurement;

import java.time.LocalDateTime;
import java.util.List;

public interface MeasurementRepository {
    List<Measurement> getMeasurements(Integer sensorId, LocalDateTime start, LocalDateTime end);
    List<Measurement> getAllForSensor(Integer sensorId);
}

