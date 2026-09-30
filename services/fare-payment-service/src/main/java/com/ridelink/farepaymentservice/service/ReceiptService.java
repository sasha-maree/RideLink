package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.client.AccountClient;
import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.dto.ReceiptData;
import com.ridelink.farepaymentservice.model.ReceiptFareSummary;
import com.ridelink.farepaymentservice.model.ReceiptPaymentSummary;
import com.ridelink.farepaymentservice.model.ReceiptPersonSummary;
import com.ridelink.farepaymentservice.exception.ResourceNotFoundException;
import com.ridelink.farepaymentservice.util.OwnershipValidator;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.model.Receipt;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import com.ridelink.farepaymentservice.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ReceiptService {

    private final FareRepository fareRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final AccountClient accountClient;
    private final RideClient rideClient;

    public ReceiptService(FareRepository fareRepository, PaymentRepository paymentRepository, ReceiptRepository receiptRepository, AccountClient accountClient, RideClient rideClient) {
        this.fareRepository = fareRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.accountClient = accountClient;
        this.rideClient = rideClient;
    }

    public ReceiptData getReceipt(String rideId, String token) {
        OwnershipValidator.validateRideOwnership(rideClient.getRideDetails(rideId));
        
        // Check if receipt already generated
        return receiptRepository.findByRideId(rideId)
                .map(this::mapToReceiptData)
                .orElseGet(() -> generateReceipt(rideId, token));
    }

    private ReceiptData generateReceipt(String rideId, String token) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found (ride may not be paid yet)"));

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new ResourceNotFoundException("Receipt not found (ride may not be paid yet)");
        }

        Fare fare = fareRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found for ride"));

        Map<String, Object> ride = rideClient.getRideDetails(rideId);
        String passengerId = (String) ride.get("passengerId");
        String driverId = (String) ride.get("driverId");
        
        Map<String, Object> passengerAcc = accountClient.getAccountDetails(passengerId, token);
        Map<String, Object> driverAcc = accountClient.getAccountDetails(driverId, token);

        Receipt receipt = new Receipt();
        receipt.setReceiptId(UUID.randomUUID().toString());
        receipt.setRideId(rideId);
        
        ReceiptPersonSummary passenger = new ReceiptPersonSummary();
        passenger.setAccountId(passengerId);
        passenger.setFirstName((String) passengerAcc.get("firstName"));
        passenger.setLastName((String) passengerAcc.get("lastName"));
        passenger.setEmail((String) passengerAcc.get("email"));
        receipt.setPassenger(passenger);

        ReceiptPersonSummary driver = new ReceiptPersonSummary();
        driver.setAccountId(driverId);
        driver.setFirstName((String) driverAcc.get("firstName"));
        driver.setLastName((String) driverAcc.get("lastName"));
        receipt.setDriver(driver);

        receipt.setPickupLocation((String) ride.get("pickupLocation"));
        receipt.setDropoffLocation((String) ride.get("dropoffLocation"));
        if (ride.get("actualDistanceKm") != null) {
            receipt.setActualDistanceKm(Double.valueOf(ride.get("actualDistanceKm").toString()));
        }
        if (ride.get("durationMinutes") != null) {
            receipt.setDurationMinutes(Integer.valueOf(ride.get("durationMinutes").toString()));
        }

        ReceiptFareSummary fareSummary = new ReceiptFareSummary();
        fareSummary.setFareId(fare.getFareId());
        fareSummary.setBaseFare(fare.getBaseFare());
        fareSummary.setDistanceFare(fare.getDistanceFare());
        fareSummary.setSurcharge(fare.getSurcharge());
        fareSummary.setTotalAmount(fare.getTotalAmount());
        fareSummary.setCurrency(fare.getCurrency());
        receipt.setFare(fareSummary);

        ReceiptPaymentSummary paymentSummary = new ReceiptPaymentSummary();
        paymentSummary.setPaymentId(payment.getPaymentId());
        paymentSummary.setPaymentMethod(payment.getPaymentMethod());
        paymentSummary.setPaymentStatus(payment.getPaymentStatus());
        paymentSummary.setProcessedAt(payment.getProcessedAt());
        receipt.setPayment(paymentSummary);

        receipt.setIssuedAt(LocalDateTime.now());

        Receipt savedReceipt = receiptRepository.save(receipt);
        return mapToReceiptData(savedReceipt);
    }

    private ReceiptData mapToReceiptData(Receipt receipt) {
        ReceiptData data = new ReceiptData();
        data.setReceiptId(receipt.getReceiptId());
        data.setRideId(receipt.getRideId());
        data.setPassenger(receipt.getPassenger());
        data.setDriver(receipt.getDriver());
        data.setPickupLocation(receipt.getPickupLocation());
        data.setDropoffLocation(receipt.getDropoffLocation());
        data.setActualDistanceKm(receipt.getActualDistanceKm());
        data.setDurationMinutes(receipt.getDurationMinutes());
        data.setFare(receipt.getFare());
        data.setPayment(receipt.getPayment());
        data.setIssuedAt(receipt.getIssuedAt());
        return data;
    }
}
