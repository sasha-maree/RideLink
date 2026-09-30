package com.ridelink.farepaymentservice.dto;

import java.time.LocalDateTime;

public class FareResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private FareData data;
    
    public FareResponse(Integer status, FareData data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.data = data;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public FareData getData() { return data; }
    public void setData(FareData data) { this.data = data; }
}
