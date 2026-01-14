package pl.szebi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.szebi.dto.PredictionRequest;
import pl.szebi.model.EnergyStats;
import pl.szebi.model.Prediction;
import pl.szebi.model.PredictionModelType;
import pl.szebi.model.Report;
import pl.szebi.repository.EnergyStatsRepository;
import pl.szebi.service.EnergyAnalyzer;
import pl.szebi.service.Predictor;
import pl.szebi.service.ReportingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PredictionController.class)
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Predictor predictor;

    @MockBean
    private EnergyAnalyzer energyAnalyzer;

    @MockBean
    private EnergyStatsRepository energyStatsRepository;

    @MockBean
    private ReportingService reportingService;

    @Test
    void testRunPrediction_Success() throws Exception {
        // Given
        PredictionRequest request = new PredictionRequest();
        request.setSensorId(1);
        request.setModelId(1);
        request.setModelType(PredictionModelType.MOVING_AVG);
        request.setHistoryDays(7);

        EnergyStats stats = new EnergyStats(
            1, 1, LocalDateTime.now().minusDays(7), LocalDateTime.now(),
            3.5, 84.0, 30660.0, 2.0, 5.0
        );

        Prediction prediction = new Prediction(
            1, 1, LocalDateTime.now(), LocalDate.now().plusDays(1),
            3.5, 1
        );

        Report report = new Report(
            1, "ANALYSIS_WITH_PREDICTION", 1, 
            LocalDateTime.now().minusDays(7), LocalDateTime.now(),
            1, 1, "Summary", new HashMap<>(), LocalDateTime.now()
        );

        doNothing().when(predictor).selectModel(any(), any());
        doNothing().when(predictor).loadModel();
        when(predictor.predictNextDayAverageConsumption(any())).thenReturn(3.5);
        when(predictor.buildPredictionEntity(any(), any())).thenReturn(prediction);
        when(energyAnalyzer.averageConsumption(any(), any())).thenReturn(stats);
        when(energyStatsRepository.save(any())).thenReturn(stats);
        when(reportingService.generateReport(any(), any(), any())).thenReturn(report);

        // When/Then
        mockMvc.perform(post("/api/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prediction.sensorId").value(1))
            .andExpect(jsonPath("$.saveSucceeded").value(true));
    }
}

