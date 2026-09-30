package com.ridelink.farepaymentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.ridelink.farepaymentservice.exception.ResourceNotFoundException;
import com.ridelink.farepaymentservice.config.JwtUtil;

import java.util.Map;

@Component
public class RideClient {
    private final RestTemplate restTemplate;
    private final String rideServiceUrl;
    private final JwtUtil jwtUtil;

    public RideClient(RestTemplate restTemplate, @Value("${ride.service.url:http://localhost:8083}") String rideServiceUrl, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.rideServiceUrl = rideServiceUrl;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> getRideDetails(String rideId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtUtil.generateFarePaymentServiceToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = rideServiceUrl + "/api/v1/rides/" + rideId;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return (Map<String, Object>) response.getBody().get("data");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Ride not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch ride details: " + e.getMessage());
        }
    }
}
