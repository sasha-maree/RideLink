package com.ridelink.farepaymentservice.dto;

import java.time.LocalDateTime;

public class PaymentResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private PaymentData data;
    
    public PaymentResponse(Integer status, PaymentData data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.data = data;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public PaymentData getData() { return data; }
    public void setData(PaymentData data) { this.data = data; }
}
