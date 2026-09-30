package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.dto.FareData;
import com.ridelink.farepaymentservice.dto.FareEstimateData;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.repository.FareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FareServiceTest {

    @Mock
    private FareRepository fareRepository;

    @Mock
    private RideClient rideClient;

    @InjectMocks
    private FareService fareService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("passenger1", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER"))));
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void estimateFare_ValidDistance_ReturnsCorrectFare() {
        FareEstimateData data = fareService.estimateFare(10.0);
        
        assertEquals(new BigDecimal("300.00"), data.getBaseFare());
        assertEquals(new BigDecimal("1000.00"), data.getDistanceFare());
        assertEquals(new BigDecimal("1300.00"), data.getEstimatedTotal());
        assertEquals("LKR", data.getCurrency());
    }

    @Test
    public void calculateFare_ValidRide_ReturnsCorrectFare() {
        when(fareRepository.findByRideId("ride1")).thenReturn(Optional.empty());
        when(rideClient.getRideDetails("ride1")).thenReturn(Map.of("status", "COMPLETED"));
        
        when(fareRepository.save(any(Fare.class))).thenAnswer(i -> {
            Fare f = i.getArgument(0);
            return f;
        });

        FareData data = fareService.calculateFare("ride1", 10.0, 15);

        assertEquals(new BigDecimal("300.00"), data.getBaseFare());
        assertEquals(new BigDecimal("1000.00"), data.getDistanceFare());
        assertEquals(new BigDecimal("1300.00"), data.getTotalAmount());
        assertEquals("LKR", data.getCurrency());
    }
    
    @Test
    public void calculateFare_ZeroKm_ReturnsBaseFareOnly() {
        when(fareRepository.findByRideId("ride2")).thenReturn(Optional.empty());
        when(rideClient.getRideDetails("ride2")).thenReturn(Map.of("status", "COMPLETED", "passengerId", "passenger1"));
        when(fareRepository.save(any(Fare.class))).thenAnswer(i -> i.getArgument(0));

        FareData data = fareService.calculateFare("ride2", 0.0, 5);

        assertEquals(new BigDecimal("300.00"), data.getBaseFare());
        assertEquals(new BigDecimal("0.00"), data.getDistanceFare());
        assertEquals(new BigDecimal("300.00"), data.getTotalAmount());
        assertEquals("LKR", data.getCurrency());
    }

    @Test
    public void calculateFare_DecimalDistance_ReturnsCorrectFare() {
        when(fareRepository.findByRideId("ride3")).thenReturn(Optional.empty());
        when(rideClient.getRideDetails("ride3")).thenReturn(Map.of("status", "COMPLETED", "passengerId", "passenger1"));
        when(fareRepository.save(any(Fare.class))).thenAnswer(i -> i.getArgument(0));

        FareData data = fareService.calculateFare("ride3", 12.5, 20);

        assertEquals(new BigDecimal("300.00"), data.getBaseFare());
        assertEquals(new BigDecimal("1250.00"), data.getDistanceFare());
        assertEquals(new BigDecimal("1550.00"), data.getTotalAmount());
    }
    
    @Test
    public void getFareByRideId_ValidOwnership_ReturnsFare() {
        Fare fare = new Fare();
        fare.setRideId("ride4");
        when(fareRepository.findByRideId("ride4")).thenReturn(Optional.of(fare));
        when(rideClient.getRideDetails("ride4")).thenReturn(Map.of("passengerId", "passenger1"));
        
        FareData data = fareService.getFareByRideId("ride4");
        assertNotNull(data);
    }
}
