package com.ridelink.rideservice.client;

import com.ridelink.rideservice.dto.AvailableDriverListResponse;
import com.ridelink.rideservice.dto.AvailableDriverSummary;
import com.ridelink.rideservice.dto.UpdateDriverAvailabilityRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class DriverVehicleClient {

    private final RestTemplate restTemplate;

    @Value("${driver.vehicle.service.url:http://localhost:8082/api/v1}")
    private String driverVehicleServiceUrl;

    public DriverVehicleClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String jwt = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (jwt != null) {
            headers.setBearerAuth(jwt);
        }
        return headers;
    }

    public List<AvailableDriverSummary> getAvailableDrivers(int limit) {
        String url = driverVehicleServiceUrl + "/drivers/available?limit=" + limit;
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        
        ResponseEntity<AvailableDriverListResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                AvailableDriverListResponse.class
        );
        
        if (response.getBody() != null && response.getBody().getData() != null) {
            return response.getBody().getData();
        }
        return List.of();
    }

    public void updateDriverAvailability(UUID driverId, String status) {
        String url = driverVehicleServiceUrl + "/drivers/" + driverId + "/availability";
        UpdateDriverAvailabilityRequest request = new UpdateDriverAvailabilityRequest(status);
        HttpEntity<UpdateDriverAvailabilityRequest> entity = new HttpEntity<>(request, getAuthHeaders());
        
        restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                Void.class
        );
    }
}
