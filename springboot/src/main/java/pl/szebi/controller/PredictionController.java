package pl.szebi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import pl.szebi.dto.ErrorResponse;
import pl.szebi.dto.PredictionRequest;
import pl.szebi.dto.PredictionResponse;
import pl.szebi.service.FastApiClient;

@RestController
@RequestMapping("/api/prediction")
@CrossOrigin(origins = "*")
public class PredictionController {
    private final FastApiClient fastApiClient;

    public PredictionController(FastApiClient fastApiClient) {
        this.fastApiClient = fastApiClient;
    }

    @PostMapping
    public ResponseEntity<?> runPrediction(@Valid @RequestBody PredictionRequest request) {
        try {
            // Wywołanie FastAPI endpointu
            PredictionResponse response = fastApiClient.post(
                "/prediction",
                request,
                PredictionResponse.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException e) {
            // Błędy 4xx z FastAPI
            if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 404) {
                return ResponseEntity.status(e.getStatusCode())
                    .body(parseErrorResponse(e));
            }
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse("CLIENT_ERROR", e.getMessage()));
        } catch (HttpServerErrorException e) {
            // Błędy 5xx z FastAPI
            return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse("SERVER_ERROR", e.getMessage()));
        } catch (RestClientException e) {
            // Błąd połączenia z FastAPI
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("FASTAPI_UNAVAILABLE", "FastAPI service is not available: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Internal server error: " + e.getMessage()));
        }
    }

    private ErrorResponse parseErrorResponse(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            return new ErrorResponse("FASTAPI_ERROR", responseBody);
        } catch (Exception ex) {
            return new ErrorResponse("FASTAPI_ERROR", e.getMessage());
        }
    }
}

