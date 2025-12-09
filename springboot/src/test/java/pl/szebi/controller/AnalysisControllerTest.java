package pl.szebi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.szebi.dto.AnalysisRequest;
import pl.szebi.model.EnergyStats;
import pl.szebi.model.Report;
import pl.szebi.repository.EnergyStatsRepository;
import pl.szebi.service.EnergyAnalyzer;
import pl.szebi.service.ReportingService;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnalysisController.class)
@ActiveProfiles("test")
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnergyAnalyzer energyAnalyzer;

    @MockBean
    private EnergyStatsRepository energyStatsRepository;

    @MockBean
    private ReportingService reportingService;

    @Test
    void testRunAnalysis_Success() throws Exception {
        // Given
        AnalysisRequest request = new AnalysisRequest();
        request.setSensorId(1);
        request.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        request.setEndTime(LocalDateTime.of(2025, 1, 2, 0, 0));

        EnergyStats stats = new EnergyStats(
            1, 1, request.getStartTime(), request.getEndTime(),
            3.5, 84.0, 30660.0, 2.0, 5.0
        );

        Report report = new Report(
            1, "ANALYSIS", 1, request.getStartTime(), request.getEndTime(),
            1, null, "Summary", new HashMap<>(), LocalDateTime.now()
        );

        when(energyAnalyzer.averageConsumption(any(), any())).thenReturn(stats);
        when(energyStatsRepository.save(any())).thenReturn(stats);
        when(reportingService.generateReport(any(), any(), any())).thenReturn(report);

        // When/Then
        mockMvc.perform(post("/api/analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stats.sensorId").value(1))
            .andExpect(jsonPath("$.saveSucceeded").value(true));
    }

    @Test
    void testRunAnalysis_InvalidTimeRange() throws Exception {
        // Given
        AnalysisRequest request = new AnalysisRequest();
        request.setSensorId(1);
        request.setStartTime(LocalDateTime.of(2025, 1, 2, 0, 0));
        request.setEndTime(LocalDateTime.of(2025, 1, 1, 0, 0)); // Invalid: end before start

        // When/Then
        mockMvc.perform(post("/api/analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
