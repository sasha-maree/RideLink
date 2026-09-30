package com.ridelink.rideservice.dto;

import com.ridelink.rideservice.model.RideStatusAction;
import jakarta.validation.constraints.NotNull;

public class UpdateRideStatusRequest {

    @NotNull(message = "Action is required")
    private RideStatusAction action;

    public UpdateRideStatusRequest() {}

    public RideStatusAction getAction() {
        return action;
    }

    public void setAction(RideStatusAction action) {
        this.action = action;
    }
}
