package pl.szebi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.szebi.dto.ErrorResponse;
import pl.szebi.model.Measurement;
import pl.szebi.service.FastApiClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
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
}

