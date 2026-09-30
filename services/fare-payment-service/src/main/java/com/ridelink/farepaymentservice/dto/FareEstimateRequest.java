package com.ridelink.farepaymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class FareEstimateRequest {
    @NotNull(message = "Estimated distance is required")
    @DecimalMin(value = "0.1", message = "Estimated distance must be at least 0.1")
    private Double estimatedDistanceKm;
    
    // Getters and Setters
    public Double getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(Double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }
}
