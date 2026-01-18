package pl.szebi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.szebi.dto.ErrorResponse;
import pl.szebi.model.Measurement;
import pl.szebi.service.FastApiClient;
import pl.szebi.symulacja.SimulationManager;
import pl.szebi.symulacja.SimulationRecord;
import pl.szebi.symulacja.Settings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/data")
// @CrossOrigin(origins = "*")
public class DataController {
    private final FastApiClient fastApiClient;

    public DataController(FastApiClient fastApiClient) {
        this.fastApiClient = fastApiClient;
    }

    @GetMapping("/measurements")
    public ResponseEntity<?> getMeasurements(
            @RequestParam Integer sensorId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        try {
            // Buduj URL z parametrami
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/data/measurements")
                .queryParam("sensorId", sensorId);
            
            if (start != null && !start.isEmpty()) {
                builder.queryParam("start", start);
            }
            if (end != null && !end.isEmpty()) {
                builder.queryParam("end", end);
            }
            
            String endpoint = builder.toUriString();
            
            // Wywołanie FastAPI endpointu
            List<?> measurements = fastApiClient.get(endpoint, List.class);
            return ResponseEntity.ok(measurements);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("FASTAPI_UNAVAILABLE", "FastAPI service is not available: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/simulation/results")
    public ResponseEntity<?> getSimulationResults() {
        try {
            // Pobierz wyniki z SimulationManager
            SimulationRecord[] records = SimulationManager.getSimulationResults();

            if (records == null || records.length == 0) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Konwertuj SimulationRecord[] na listę map kompatybilną z frontendem
            List<Map<String, Object>> results = new ArrayList<>();
            for (SimulationRecord record : records) {
                if (record != null) {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", record.getId());
                    map.put("periodNumber", record.getPeriodNumber());
                    map.put("simulationDate", record.getSimulationDate() != null ? record.getSimulationDate().toString() : null);
                    map.put("periodStart", record.getPeriodStart() != null ? record.getPeriodStart().toString() : null);
                    map.put("periodEnd", record.getPeriodEnd() != null ? record.getPeriodEnd().toString() : null);
                    map.put("gridConsumption", record.getGridConsumption());
                    map.put("gridFeedIn", record.getGridFeedIn());
                    map.put("pvProduction", record.getPvProduction());
                    map.put("batteryLevel", record.getBatteryLevel());
                    map.put("energyStored", record.getEnergyStored());
                    map.put("sunlightIntensity", record.getSunlightIntensity());
                    map.put("panelPower", record.getPanelPower());
                    map.put("batteryCapacity", record.getBatteryCapacity());
                    results.add(map);
                }
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/simulation/run")
    public ResponseEntity<?> runSimulation() {
        try {
            // Utworzenie ustawień systemu
            Settings settings = new Settings();
            settings.setPanelPower(5.0);
            settings.setBatteryCapacity(100.0);

            // Utworzenie managera symulacji
            SimulationManager manager = new SimulationManager(settings);

            // Wykonanie symulacji dla dzisiejszej daty
            java.time.LocalDate today = java.time.LocalDate.now();
            java.util.List<SimulationRecord> records = manager.simulateDay(today);

            // Zwróć informację o sukcesie
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Simulation completed successfully");
            response.put("recordCount", records.size());
            response.put("date", today.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("SIMULATION_ERROR", "Failed to run simulation: " + e.getMessage()));
        }
    }
}

