package com.ridelink.driver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.ZonedDateTime;
import java.util.UUID;

@Document(collection = "driver_profiles")
public class DriverProfile {

    @Id
    private UUID driverId; // This is the accountId from Account Service

    @Indexed(unique = true)
    private String licenseNumber;

    private Double rating;

    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    private UUID activeVehicleId;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    public DriverProfile() {
    }

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
