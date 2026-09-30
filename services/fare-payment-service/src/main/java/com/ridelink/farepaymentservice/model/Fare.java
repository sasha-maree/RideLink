package com.ridelink.farepaymentservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "fares")
public class Fare {
    @Id
    private String id;
    private String fareId; // UUID
    private String rideId; // UUID
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal surcharge;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime calculatedAt;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFareId() { return fareId; }
    public void setFareId(String fareId) { this.fareId = fareId; }
    
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    
    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
    
    public BigDecimal getDistanceFare() { return distanceFare; }
    public void setDistanceFare(BigDecimal distanceFare) { this.distanceFare = distanceFare; }
    
    public BigDecimal getSurcharge() { return surcharge; }
    public void setSurcharge(BigDecimal surcharge) { this.surcharge = surcharge; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
