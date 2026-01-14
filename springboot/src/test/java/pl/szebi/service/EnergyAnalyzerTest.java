package pl.szebi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.szebi.model.EnergyStats;
import pl.szebi.model.Measurement;
import pl.szebi.model.TimeRange;
import pl.szebi.repository.MeasurementRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyAnalyzerTest {

    @Mock
    private MeasurementRepository measurementRepository;

    private EnergyAnalyzer energyAnalyzer;

    @BeforeEach
    void setUp() {
        energyAnalyzer = new EnergyAnalyzer(measurementRepository);
    }

    @Test
    void testAverageConsumption_WithValidData() {
        // Given
        Integer sensorId = 1;
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 2, 0);
        TimeRange timeRange = new TimeRange(start, end);

        List<Measurement> measurements = Arrays.asList(
            new Measurement(1, start, 1000.0, 0.1, 2.5),
            new Measurement(2, start.plusHours(1), 2000.0, 0.2, 3.5),
            new Measurement(3, end, 1500.0, 0.15, 4.0)
        );

        when(measurementRepository.getMeasurements(eq(sensorId), eq(start), eq(end)))
            .thenReturn(measurements);

        // When
        EnergyStats stats = energyAnalyzer.averageConsumption(sensorId, timeRange);

        // Then
        assertNotNull(stats);
        assertEquals(sensorId, stats.getSensorId());
        assertEquals(3.33, stats.getAvg(), 0.1);
        assertEquals(2.5, stats.getMin(), 0.01);
        assertEquals(4.0, stats.getMax(), 0.01);
    }

    @Test
    void testAverageConsumption_NoData() {
        // Given
        Integer sensorId = 1;
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 2, 0);
        TimeRange timeRange = new TimeRange(start, end);

        when(measurementRepository.getMeasurements(eq(sensorId), eq(start), eq(end)))
            .thenReturn(Collections.emptyList());

        // When/Then
        assertThrows(EnergyAnalyzer.NoDataException.class, () -> {
            energyAnalyzer.averageConsumption(sensorId, timeRange);
        });
    }

    @Test
    void testLowestConsumption() {
        // Given
        Integer sensorId = 1;
        List<Measurement> measurements = Arrays.asList(
            new Measurement(1, LocalDateTime.now(), 1000.0, 0.1, 2.5),
            new Measurement(2, LocalDateTime.now().plusHours(1), 2000.0, 0.2, 1.5),
            new Measurement(3, LocalDateTime.now().plusHours(2), 1500.0, 0.15, 4.0)
        );

        when(measurementRepository.getAllForSensor(sensorId))
            .thenReturn(measurements);

        // When
        EnergyStats stats = energyAnalyzer.lowestConsumption(sensorId);

        // Then
        assertNotNull(stats);
        assertEquals(1.5, stats.getMin(), 0.01);
        assertEquals(1.5, stats.getAvg(), 0.01);
    }

    @Test
    void testHighestConsumption() {
        // Given
        Integer sensorId = 1;
        List<Measurement> measurements = Arrays.asList(
            new Measurement(1, LocalDateTime.now(), 1000.0, 0.1, 2.5),
            new Measurement(2, LocalDateTime.now().plusHours(1), 2000.0, 0.2, 4.5),
            new Measurement(3, LocalDateTime.now().plusHours(2), 1500.0, 0.15, 3.0)
        );

        when(measurementRepository.getAllForSensor(sensorId))
            .thenReturn(measurements);

        // When
        EnergyStats stats = energyAnalyzer.highestConsumption(sensorId);

        // Then
        assertNotNull(stats);
        assertEquals(4.5, stats.getMax(), 0.01);
        assertEquals(4.5, stats.getAvg(), 0.01);
    }
}

