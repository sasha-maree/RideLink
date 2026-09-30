package com.ridelink.farepaymentservice.dto;

import java.time.LocalDateTime;

public class ReceiptResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private ReceiptData data;
    
    public ReceiptResponse(Integer status, ReceiptData data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.data = data;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public ReceiptData getData() { return data; }
    public void setData(ReceiptData data) { this.data = data; }
}
