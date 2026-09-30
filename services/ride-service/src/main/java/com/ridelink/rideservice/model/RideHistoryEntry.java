package com.ridelink.rideservice.model;

import java.time.LocalDateTime;

public class RideHistoryEntry {

    private RideStatus fromStatus;
    private RideStatus toStatus;
    private LocalDateTime changedAt;

    public RideHistoryEntry() {}

    public RideHistoryEntry(RideStatus fromStatus, RideStatus toStatus, LocalDateTime changedAt) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    public RideStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(RideStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public RideStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(RideStatus toStatus) {
        this.toStatus = toStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
