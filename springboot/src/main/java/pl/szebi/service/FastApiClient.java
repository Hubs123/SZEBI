package pl.szebi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class FastApiClient {
    private final RestTemplate restTemplate;
    
    @Value("${fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    public FastApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T post(String endpoint, Object request, Class<T> responseType) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<T> response = restTemplate.exchange(
            fastApiBaseUrl + endpoint,
            HttpMethod.POST,
            entity,
            responseType
        );
        return response.getBody();
    }

    public <T> T get(String endpoint, Class<T> responseType) throws RestClientException {
        return restTemplate.getForObject(fastApiBaseUrl + endpoint, responseType);
    }
}

