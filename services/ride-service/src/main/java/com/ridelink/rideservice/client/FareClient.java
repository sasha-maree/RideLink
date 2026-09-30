package com.ridelink.rideservice.client;

import com.ridelink.rideservice.config.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class FareClient {

    private final RestTemplate restTemplate;
    private final String farePaymentServiceUrl;
    private final JwtUtil jwtUtil;

    public FareClient(RestTemplate restTemplate, 
                      @Value("${fare.payment.service.url:http://localhost:8084}") String farePaymentServiceUrl,
                      JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.farePaymentServiceUrl = farePaymentServiceUrl;
        this.jwtUtil = jwtUtil;
    }

    public void calculateFare(UUID rideId, Double actualDistanceKm, Integer durationMinutes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtUtil.generateRideServiceToken());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("rideId", rideId.toString());
            requestBody.put("actualDistanceKm", actualDistanceKm);
            requestBody.put("durationMinutes", durationMinutes);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = farePaymentServiceUrl + "/api/v1/fares/calculate";
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (Exception e) {
            // Log error but don't fail the complete ride transaction completely if possible,
            // though depending on business rules, maybe we should.
            // For now just wrap in RuntimeException
            throw new RuntimeException("Failed to call Fare Payment Service: " + e.getMessage());
        }
    }
}
