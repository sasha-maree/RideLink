package com.ridelink.driver.dto;

import com.ridelink.driver.model.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public class SetAvailabilityRequest {

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus availabilityStatus;

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
