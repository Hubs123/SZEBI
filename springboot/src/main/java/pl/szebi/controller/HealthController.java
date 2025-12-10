package pl.szebi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import pl.szebi.service.FastApiClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    private final FastApiClient fastApiClient;

    public HealthController(FastApiClient fastApiClient) {
        this.fastApiClient = fastApiClient;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        
        // Sprawdź również status FastAPI
        try {
            Map<String, Object> fastApiHealth = fastApiClient.get("/health", Map.class);
            response.put("fastapi", fastApiHealth);
            response.put("fastapiStatus", "UP");
        } catch (RestClientException e) {
            response.put("fastapiStatus", "DOWN");
            response.put("fastapiError", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}

