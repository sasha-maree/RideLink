package com.ridelink.rideservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RideHistoryResponse {
    private LocalDateTime timestamp;
    private int status;
    private RideHistoryData data;

    public RideHistoryResponse() {}

    public RideHistoryResponse(LocalDateTime timestamp, int status, UUID rideId, List<RideHistoryEntryDto> history) {
        this.timestamp = timestamp;
        this.status = status;
        this.data = new RideHistoryData(rideId, history);
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public RideHistoryData getData() { return data; }
    public void setData(RideHistoryData data) { this.data = data; }

    public static class RideHistoryData {
        private UUID rideId;
        private List<RideHistoryEntryDto> history;

        public RideHistoryData() {}

        public RideHistoryData(UUID rideId, List<RideHistoryEntryDto> history) {
            this.rideId = rideId;
            this.history = history;
        }

        public UUID getRideId() { return rideId; }
        public void setRideId(UUID rideId) { this.rideId = rideId; }

        public List<RideHistoryEntryDto> getHistory() { return history; }
        public void setHistory(List<RideHistoryEntryDto> history) { this.history = history; }
    }
}
