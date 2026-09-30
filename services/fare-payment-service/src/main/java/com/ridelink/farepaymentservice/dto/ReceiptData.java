package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.ReceiptFareSummary;
import com.ridelink.farepaymentservice.model.ReceiptPaymentSummary;
import com.ridelink.farepaymentservice.model.ReceiptPersonSummary;

import java.time.LocalDateTime;

public class ReceiptData {
    private String receiptId;
    private String rideId;
    private ReceiptPersonSummary passenger;
    private ReceiptPersonSummary driver;
    private String pickupLocation;
    private String dropoffLocation;
    private Double actualDistanceKm;
    private Integer durationMinutes;
    private ReceiptFareSummary fare;
    private ReceiptPaymentSummary payment;
    private LocalDateTime issuedAt;
    
    // Getters and Setters
    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    
    public ReceiptPersonSummary getPassenger() { return passenger; }
    public void setPassenger(ReceiptPersonSummary passenger) { this.passenger = passenger; }
    
    public ReceiptPersonSummary getDriver() { return driver; }
    public void setDriver(ReceiptPersonSummary driver) { this.driver = driver; }
    
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    
    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }
    
    public Double getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(Double actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public ReceiptFareSummary getFare() { return fare; }
    public void setFare(ReceiptFareSummary fare) { this.fare = fare; }
    
    public ReceiptPaymentSummary getPayment() { return payment; }
    public void setPayment(ReceiptPaymentSummary payment) { this.payment = payment; }
    
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
