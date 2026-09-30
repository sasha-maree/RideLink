package com.ridelink.rideservice.dto;

import com.ridelink.rideservice.model.RideStatus;

import java.time.LocalDateTime;

public class RideHistoryEntryDto {
    private RideStatus fromStatus;
    private RideStatus toStatus;
    private LocalDateTime changedAt;

    public RideHistoryEntryDto() {}

    public RideHistoryEntryDto(RideStatus fromStatus, RideStatus toStatus, LocalDateTime changedAt) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    public RideStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(RideStatus fromStatus) { this.fromStatus = fromStatus; }

    public RideStatus getToStatus() { return toStatus; }
    public void setToStatus(RideStatus toStatus) { this.toStatus = toStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
