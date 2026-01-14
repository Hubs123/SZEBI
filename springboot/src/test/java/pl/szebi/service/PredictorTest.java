package pl.szebi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.szebi.model.Measurement;
import pl.szebi.model.Prediction;
import pl.szebi.model.PredictionConfig;
import pl.szebi.model.PredictionModelType;
import pl.szebi.repository.MeasurementRepository;
import pl.szebi.repository.PredictionRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictorTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private PredictionRepository predictionRepository;

    private Predictor predictor;
    private PredictionConfig config;

    @BeforeEach
    void setUp() {
        config = new PredictionConfig(7, 30);
        predictor = new Predictor(measurementRepository, predictionRepository, config);
    }

    @Test
    void testSelectModel() {
        // When
        predictor.selectModel(1, PredictionModelType.MOVING_AVG);
        predictor.loadModel();

        // Then - no exception should be thrown
        assertDoesNotThrow(() -> predictor.loadModel());
    }

    @Test
    void testLoadModel_WithoutSelection() {
        // Then
        assertThrows(Predictor.ModelNotSelectedException.class, () -> {
            predictor.loadModel();
        });
    }

    @Test
    void testPredictNextDayAverageConsumption_SimpleAvg() {
        // Given
        Integer sensorId = 1;
        predictor.selectModel(1, PredictionModelType.SIMPLE_AVG);
        predictor.loadModel();

        List<Measurement> measurements = Arrays.asList(
            new Measurement(1, LocalDateTime.now().minusDays(1), 1000.0, 0.1, 2.5),
            new Measurement(2, LocalDateTime.now().minusDays(2), 2000.0, 0.2, 3.5),
            new Measurement(3, LocalDateTime.now().minusDays(3), 1500.0, 0.15, 4.0)
        );

        when(measurementRepository.getMeasurements(any(), any(), any()))
            .thenReturn(measurements);

        Prediction savedPrediction = new Prediction(1, sensorId, LocalDateTime.now(), 
            null, 3.33, 1);
        when(predictionRepository.save(any(Prediction.class)))
            .thenReturn(savedPrediction);

        // When
        Double predictedValue = predictor.predictNextDayAverageConsumption(sensorId);
        Prediction prediction = predictor.buildPredictionEntity(sensorId, predictedValue);

        // Then
        assertNotNull(predictedValue);
        assertEquals(3.33, predictedValue, 0.1);
        assertNotNull(prediction);
    }

    @Test
    void testPredictNextDayAverageConsumption_NoHistoryData() {
        // Given
        Integer sensorId = 1;
        predictor.selectModel(1, PredictionModelType.SIMPLE_AVG);
        predictor.loadModel();

        when(measurementRepository.getMeasurements(any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // When/Then
        assertThrows(Predictor.NoHistoryDataException.class, () -> {
            predictor.predictNextDayAverageConsumption(sensorId);
        });
    }

    @Test
    void testPredictNextDayAverageConsumption_ModelNotSelected() {
        // Given
        Integer sensorId = 1;

        // When/Then
        assertThrows(Predictor.ModelNotSelectedException.class, () -> {
            predictor.predictNextDayAverageConsumption(sensorId);
        });
    }
}

