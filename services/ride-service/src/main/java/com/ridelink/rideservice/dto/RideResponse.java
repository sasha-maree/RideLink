package com.ridelink.rideservice.dto;

import java.time.LocalDateTime;

public class RideResponse {
    private LocalDateTime timestamp;
    private int status;
    private RideData data;

    public RideResponse() {}

    public RideResponse(LocalDateTime timestamp, int status, RideData data) {
        this.timestamp = timestamp;
        this.status = status;
        this.data = data;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public RideData getData() { return data; }
    public void setData(RideData data) { this.data = data; }
}
