package com.ridelink.rideservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AvailableDriverListResponse {
    private String timestamp;
    private int status;
    private List<AvailableDriverSummary> data;

    public AvailableDriverListResponse() {}

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public List<AvailableDriverSummary> getData() { return data; }
    public void setData(List<AvailableDriverSummary> data) { this.data = data; }
}
