package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.dto.PaymentData;
import com.ridelink.farepaymentservice.exception.PaymentAlreadyCompletedException;
import com.ridelink.farepaymentservice.exception.PaymentFailedException;
import com.ridelink.farepaymentservice.exception.ResourceNotFoundException;
import com.ridelink.farepaymentservice.exception.RideNotCompletedException;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentMethod;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.util.OwnershipValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareRepository fareRepository;
    private final RideClient rideClient;

    public PaymentService(PaymentRepository paymentRepository, FareRepository fareRepository, RideClient rideClient) {
        this.paymentRepository = paymentRepository;
        this.fareRepository = fareRepository;
        this.rideClient = rideClient;
    }

    public PaymentData processPayment(String rideId, PaymentMethod paymentMethod) {
        OwnershipValidator.validatePassengerOwnership(rideClient.getRideDetails(rideId));
        
        paymentRepository.findByRideId(rideId).ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                throw new PaymentAlreadyCompletedException("A payment has already been successfully processed for this ride.");
            }
        });

        Fare fare = fareRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found for ride: " + rideId));

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setRideId(rideId);
        payment.setFareId(fare.getFareId());
        payment.setAmount(fare.getTotalAmount());
        payment.setCurrency(fare.getCurrency());
        payment.setPaymentMethod(paymentMethod);
        
        // Simulated failure mechanism (N-05)
        if (paymentMethod == PaymentMethod.WALLET) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment); // Save the failed payment so we know it failed
            throw new PaymentFailedException("Simulated payment processing failed. Please try again.");
        }
        
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentData(savedPayment);
    }

    public PaymentData getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        
        OwnershipValidator.validateRideOwnership(rideClient.getRideDetails(payment.getRideId()));

        return mapToPaymentData(payment);
    }

    private PaymentData mapToPaymentData(Payment payment) {
        PaymentData data = new PaymentData();
        data.setPaymentId(payment.getPaymentId());
        data.setRideId(payment.getRideId());
        data.setFareId(payment.getFareId());
        data.setAmount(payment.getAmount());
        data.setCurrency(payment.getCurrency());
        data.setPaymentMethod(payment.getPaymentMethod());
        data.setPaymentStatus(payment.getPaymentStatus());
        data.setProcessedAt(payment.getProcessedAt());
        return data;
    }
}
