package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.client.AccountClient;
import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.exception.FareAlreadyCalculatedException;
import com.ridelink.farepaymentservice.exception.PaymentAlreadyCompletedException;
import com.ridelink.farepaymentservice.exception.PaymentFailedException;
import com.ridelink.farepaymentservice.exception.RideNotCompletedException;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentMethod;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import com.ridelink.farepaymentservice.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Phase 6 hardened tests for:
 * - N-05: Simulated payment failure (WALLET method)
 * - Duplicate payment rejection
 * - Fare already calculated rejection
 * - Ride not completed rejection
 * - Payment before fare exists
 */
@ExtendWith(MockitoExtension.class)
public class FarePaymentHardenedTest {

    @Mock private FareRepository fareRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ReceiptRepository receiptRepository;
    @Mock private RideClient rideClient;
    @Mock private AccountClient accountClient;

    private FareService fareService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        fareService = new FareService(fareRepository, rideClient);
        paymentService = new PaymentService(paymentRepository, fareRepository, rideClient);

        // Set up a PASSENGER authentication context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "passenger-uuid-001", "token",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PASSENGER"))));
    }

    // ─── N-05: Simulated Payment Failure ─────────────────────────────────────────

    @Test
    void n05_walletPayment_simulatesFailure() {
        when(rideClient.getRideDetails("ride1"))
                .thenReturn(Map.of("passengerId", "passenger-uuid-001", "status", "COMPLETED"));
        when(paymentRepository.findByRideId("ride1")).thenReturn(Optional.empty());

        Fare fare = testFare("ride1");
        when(fareRepository.findByRideId("ride1")).thenReturn(Optional.of(fare));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentFailedException ex = assertThrows(PaymentFailedException.class,
                () -> paymentService.processPayment("ride1", PaymentMethod.WALLET));

        assertTrue(ex.getMessage().toLowerCase().contains("fail") || ex.getMessage().toLowerCase().contains("simulated"),
                "Expected a failure-describing message, got: " + ex.getMessage());
    }

    @Test
    void n05_walletPayment_failedPaymentRecordIsSaved() {
        // Verify that a FAILED payment is recorded for the N-05 audit trail
        when(rideClient.getRideDetails("ride2"))
                .thenReturn(Map.of("passengerId", "passenger-uuid-001", "status", "COMPLETED"));
        when(paymentRepository.findByRideId("ride2")).thenReturn(Optional.empty());

        Fare fare = testFare("ride2");
        when(fareRepository.findByRideId("ride2")).thenReturn(Optional.of(fare));

        // Capture the saved payment
        org.mockito.ArgumentCaptor<Payment> captor = org.mockito.ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        assertThrows(PaymentFailedException.class,
                () -> paymentService.processPayment("ride2", PaymentMethod.WALLET));

        // The saved payment MUST have FAILED status (not COMPLETED or null)
        Payment savedPayment = captor.getValue();
        assertEquals(PaymentStatus.FAILED, savedPayment.getPaymentStatus(),
                "A WALLET payment failure must be persisted with FAILED status");
    }

    @Test
    void n05_cardPayment_succeeds() {
        when(rideClient.getRideDetails("ride3"))
                .thenReturn(Map.of("passengerId", "passenger-uuid-001", "status", "COMPLETED"));
        when(paymentRepository.findByRideId("ride3")).thenReturn(Optional.empty());

        Fare fare = testFare("ride3");
        when(fareRepository.findByRideId("ride3")).thenReturn(Optional.of(fare));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        var data = paymentService.processPayment("ride3", PaymentMethod.CARD);

        assertEquals(PaymentStatus.COMPLETED, data.getPaymentStatus());
        assertEquals(new BigDecimal("1300.00"), data.getAmount());
    }

    // ─── Duplicate Payment ────────────────────────────────────────────────────────

    @Test
    void duplicatePayment_completedPaymentExists_throws() {
        when(rideClient.getRideDetails("ride4"))
                .thenReturn(Map.of("passengerId", "passenger-uuid-001"));

        Payment existingPayment = new Payment();
        existingPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByRideId("ride4")).thenReturn(Optional.of(existingPayment));

        assertThrows(PaymentAlreadyCompletedException.class,
                () -> paymentService.processPayment("ride4", PaymentMethod.CARD));
    }

    @Test
    void duplicatePayment_failedPaymentExists_canRetry() {
        // A previously FAILED payment should not block a retry
        when(rideClient.getRideDetails("ride5"))
                .thenReturn(Map.of("passengerId", "passenger-uuid-001", "status", "COMPLETED"));

        Payment failedPayment = new Payment();
        failedPayment.setPaymentStatus(PaymentStatus.FAILED);
        when(paymentRepository.findByRideId("ride5")).thenReturn(Optional.of(failedPayment));

        Fare fare = testFare("ride5");
        when(fareRepository.findByRideId("ride5")).thenReturn(Optional.of(fare));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // A failed payment should allow a retry (no exception expected for CARD)
        assertDoesNotThrow(() -> paymentService.processPayment("ride5", PaymentMethod.CARD));
    }

    // ─── Fare Already Calculated ─────────────────────────────────────────────────

    @Test
    void calculateFare_alreadyCalculated_throws() {
        Fare existingFare = testFare("ride6");
        when(fareRepository.findByRideId("ride6")).thenReturn(Optional.of(existingFare));

        assertThrows(FareAlreadyCalculatedException.class,
                () -> fareService.calculateFare("ride6", 10.0, 20));
    }

    // ─── Ride Not Completed ───────────────────────────────────────────────────────

    @Test
    void calculateFare_rideNotCompleted_throws() {
        when(fareRepository.findByRideId("ride7")).thenReturn(Optional.empty());
        when(rideClient.getRideDetails("ride7")).thenReturn(Map.of("status", "IN_PROGRESS"));

        assertThrows(RideNotCompletedException.class,
                () -> fareService.calculateFare("ride7", 10.0, 20));
    }

    // ─── Fare Formula Verification ────────────────────────────────────────────────

    @Test
    void fareFormula_baseFare300_ratePerKm100() {
        // Authoritative: baseFare=300.00 LKR, ratePerKm=100.00 LKR
        // Example: 12.5km ride = 300 + (12.5 × 100) = 1550.00 LKR
        when(fareRepository.findByRideId("ride8")).thenReturn(Optional.empty());
        when(rideClient.getRideDetails("ride8")).thenReturn(Map.of("status", "COMPLETED"));
        when(fareRepository.save(any(Fare.class))).thenAnswer(i -> i.getArgument(0));

        var data = fareService.calculateFare("ride8", 12.5, 25);

        assertEquals(new BigDecimal("300.00"), data.getBaseFare(),
                "Base fare must be 300.00 LKR per contract OW-05");
        assertEquals(new BigDecimal("1250.00"), data.getDistanceFare(),
                "Distance fare for 12.5km at 100.00/km must be 1250.00 LKR");
        assertEquals(new BigDecimal("1550.00"), data.getTotalAmount(),
                "Total fare for 12.5km ride must be 1550.00 LKR per contract example");
        assertEquals("LKR", data.getCurrency());
        assertEquals(new BigDecimal("0.00"), data.getSurcharge(), "Default surcharge must be 0.00");
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    private Fare testFare(String rideId) {
        Fare fare = new Fare();
        fare.setFareId("fare-" + rideId);
        fare.setRideId(rideId);
        fare.setTotalAmount(new BigDecimal("1300.00"));
        fare.setCurrency("LKR");
        return fare;
    }
}
