package com.ridelink.rideservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public class CreateRideRequest {

    @NotBlank(message = "Pickup location is required")
    @Size(min = 3, max = 200, message = "Pickup location must be between 3 and 200 characters")
    private String pickupLocation;

    @NotBlank(message = "Dropoff location is required")
    @Size(min = 3, max = 200, message = "Dropoff location must be between 3 and 200 characters")
    private String dropoffLocation;

    @Min(value = 0, message = "Estimated distance must be positive")
    private Double estimatedDistanceKm;

    public CreateRideRequest() {}

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public Double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public void setEstimatedDistanceKm(Double estimatedDistanceKm) {
        this.estimatedDistanceKm = estimatedDistanceKm;
    }
}
