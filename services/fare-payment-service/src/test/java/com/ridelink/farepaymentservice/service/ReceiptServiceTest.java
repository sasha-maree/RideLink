package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.client.AccountClient;
import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.dto.ReceiptData;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.model.Receipt;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import com.ridelink.farepaymentservice.repository.ReceiptRepository;
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

public class ReceiptServiceTest {

    @Mock
    private FareRepository fareRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private RideClient rideClient;

    @InjectMocks
    private ReceiptService receiptService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("passenger1", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER"))));
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void getReceipt_GeneratesAndReturnsReceipt() {
        when(rideClient.getRideDetails("ride1")).thenReturn(Map.of("passengerId", "passenger1", "driverId", "driver1", "pickupLocation", "A", "dropoffLocation", "B"));
        when(receiptRepository.findByRideId("ride1")).thenReturn(Optional.empty());
        
        Payment payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByRideId("ride1")).thenReturn(Optional.of(payment));
        
        Fare fare = new Fare();
        fare.setTotalAmount(new BigDecimal("1300.00"));
        fare.setCurrency("LKR");
        when(fareRepository.findByRideId("ride1")).thenReturn(Optional.of(fare));
        
        when(accountClient.getAccountDetails("passenger1", "token")).thenReturn(Map.of("firstName", "John", "lastName", "Doe"));
        when(accountClient.getAccountDetails("driver1", "token")).thenReturn(Map.of("firstName", "Jane", "lastName", "Smith"));
        
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(i -> i.getArgument(0));

        ReceiptData data = receiptService.getReceipt("ride1", "token");

        assertNotNull(data);
        assertEquals("John", data.getPassenger().getFirstName());
        assertEquals("Jane", data.getDriver().getFirstName());
        assertEquals(new BigDecimal("1300.00"), data.getFare().getTotalAmount());
        assertEquals("LKR", data.getFare().getCurrency());
    }
}
