package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.dto.PaymentData;
import com.ridelink.farepaymentservice.exception.PaymentAlreadyCompletedException;
import com.ridelink.farepaymentservice.exception.PaymentFailedException;
import com.ridelink.farepaymentservice.exception.UnauthorizedAccessException;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentMethod;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FareRepository fareRepository;

    @Mock
    private RideClient rideClient;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("passenger1", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER"))));
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void processPayment_Success() {
        when(rideClient.getRideDetails("ride1")).thenReturn(Map.of("passengerId", "passenger1"));
        when(paymentRepository.findByRideId("ride1")).thenReturn(Optional.empty());
        Fare fare = new Fare();
        fare.setFareId("fare1");
        fare.setTotalAmount(new BigDecimal("1300.00"));
        fare.setCurrency("LKR");
        when(fareRepository.findByRideId("ride1")).thenReturn(Optional.of(fare));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentData data = paymentService.processPayment("ride1", PaymentMethod.CARD);

        assertEquals("fare1", data.getFareId());
        assertEquals(new BigDecimal("1300.00"), data.getAmount());
        assertEquals("LKR", data.getCurrency());
        assertEquals(PaymentStatus.COMPLETED, data.getPaymentStatus());
    }

    @Test
    public void processPayment_N05_SimulatedFailure() {
        when(rideClient.getRideDetails("ride1")).thenReturn(Map.of("passengerId", "passenger1"));
        when(paymentRepository.findByRideId("ride1")).thenReturn(Optional.empty());
        Fare fare = new Fare();
        fare.setFareId("fare1");
        fare.setTotalAmount(new BigDecimal("1300.00"));
        fare.setCurrency("LKR");
        when(fareRepository.findByRideId("ride1")).thenReturn(Optional.of(fare));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(PaymentFailedException.class, () -> paymentService.processPayment("ride1", PaymentMethod.WALLET));
    }

    @Test
    public void processPayment_DuplicatePayment_ThrowsException() {
        when(rideClient.getRideDetails("ride1")).thenReturn(Map.of("passengerId", "passenger1"));
        Payment existingPayment = new Payment();
        existingPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByRideId("ride1")).thenReturn(Optional.of(existingPayment));

        assertThrows(PaymentAlreadyCompletedException.class, () -> paymentService.processPayment("ride1", PaymentMethod.CARD));
    }

    @Test
    public void processPayment_WrongPassenger_ThrowsUnauthorized() {
        when(rideClient.getRideDetails("ride1")).thenReturn(Map.of("passengerId", "passenger2"));

        assertThrows(UnauthorizedAccessException.class, () -> paymentService.processPayment("ride1", PaymentMethod.CARD));
    }
}
