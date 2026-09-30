package com.ridelink.rideservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AvailableDriverSummary {
    private UUID driverId;
    private UUID vehicleId;
    private Double rating;
    private String availabilityStatus;

    public AvailableDriverSummary() {
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
