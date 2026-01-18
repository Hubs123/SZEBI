package pl.szebi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import pl.szebi.dto.ErrorResponse;
import pl.szebi.dto.ReportDto;
import pl.szebi.service.FastApiClient;

@RestController
@RequestMapping("/api/reports")
// @CrossOrigin(origins = "*")
public class ReportController {
    private final FastApiClient fastApiClient;

    public ReportController(FastApiClient fastApiClient) {
        this.fastApiClient = fastApiClient;
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReport(@PathVariable Integer reportId) {
        try {
            // Wywołanie FastAPI endpointu
            ReportDto response = fastApiClient.get(
                "/reports/" + reportId,
                ReportDto.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("REPORT_NOT_FOUND", "Report " + reportId + " not found"));
            }
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse("CLIENT_ERROR", e.getMessage()));
        } catch (HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse("SERVER_ERROR", e.getMessage()));
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("FASTAPI_UNAVAILABLE", "FastAPI service is not available: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Internal server error: " + e.getMessage()));
        }
    }
}

