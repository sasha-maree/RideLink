package com.ridelink.driver.dto;

import com.ridelink.driver.model.AvailabilityStatus;
import java.time.ZonedDateTime;
import java.util.UUID;

public class DriverProfileData {
    private UUID driverId;
    private String licenseNumber;
    private Double rating;
    private AvailabilityStatus availabilityStatus;
    private UUID activeVehicleId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public UUID getActiveVehicleId() {
        return activeVehicleId;
    }

    public void setActiveVehicleId(UUID activeVehicleId) {
        this.activeVehicleId = activeVehicleId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
