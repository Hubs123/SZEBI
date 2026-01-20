//package pl.szebi.repository.impl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import pl.szebi.model.Measurement;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class InMemoryMeasurementRepositoryTest {
//
//    private InMemoryMeasurementRepository repository;
//
//    @BeforeEach
//    void setUp() {
//        repository = new InMemoryMeasurementRepository();
//    }
//
//    @Test
//    void testGetMeasurements_WithTimeRange() {
//        // Given
//        Integer sensorId = 1;
//        LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 11, 2, 0, 0);
//
//        // When
//        List<Measurement> measurements = repository.getMeasurements(sensorId, start, end);
//
//        // Then
//        assertNotNull(measurements);
//        assertFalse(measurements.isEmpty());
//        assertTrue(measurements.size() <= 25); // 24 hours + 1
//        measurements.forEach(m -> {
//            assertFalse(m.getTimestamp().isBefore(start));
//            assertFalse(m.getTimestamp().isAfter(end));
//        });
//    }
//
//    @Test
//    void testGetAllForSensor() {
//        // Given
//        Integer sensorId = 1;
//
//        // When
//        List<Measurement> measurements = repository.getAllForSensor(sensorId);
//
//        // Then
//        assertNotNull(measurements);
//        assertFalse(measurements.isEmpty());
//        // Should have 60 days * 24 hours = 1440 measurements
//        assertEquals(1440, measurements.size());
//    }
//
//    @Test
//    void testGetMeasurements_EmptyRange() {
//        // Given
//        Integer sensorId = 1;
//        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 0, 0);
//        LocalDateTime end = LocalDateTime.of(2030, 1, 2, 0, 0);
//
//        // When
//        List<Measurement> measurements = repository.getMeasurements(sensorId, start, end);
//
//        // Then
//        assertNotNull(measurements);
//        assertTrue(measurements.isEmpty());
//    }
//}
//
