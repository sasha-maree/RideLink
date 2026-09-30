package com.ridelink.farepaymentservice.dto;

import java.time.LocalDateTime;

public class FareEstimateResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private FareEstimateData data;
    
    public FareEstimateResponse(Integer status, FareEstimateData data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.data = data;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public FareEstimateData getData() { return data; }
    public void setData(FareEstimateData data) { this.data = data; }
}
