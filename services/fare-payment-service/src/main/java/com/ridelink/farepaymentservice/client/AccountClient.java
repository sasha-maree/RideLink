package com.ridelink.farepaymentservice.client;

import com.ridelink.farepaymentservice.config.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import com.ridelink.farepaymentservice.exception.ResourceNotFoundException;

import java.util.Map;

@Component
public class AccountClient {
    private final RestTemplate restTemplate;
    private final String accountServiceUrl;
    private final JwtUtil jwtUtil;

    public AccountClient(RestTemplate restTemplate,
                         @Value("${account.service.url:http://localhost:8081}") String accountServiceUrl,
                         JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Retrieves account details using the FARE_PAYMENT_SERVICE service JWT.
     * The callerToken parameter is kept for API compatibility but is not used for authentication;
     * the service JWT is used instead (OS-05 architecture).
     */
    public Map<String, Object> getAccountDetails(String accountId, String callerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            // Use the FARE_PAYMENT_SERVICE JWT for internal service-to-service calls
            headers.setBearerAuth(jwtUtil.generateFarePaymentServiceToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = accountServiceUrl + "/api/v1/accounts/" + accountId;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return (Map<String, Object>) response.getBody().get("data");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account details: " + e.getMessage());
        }
    }
}
