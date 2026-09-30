package com.ridelink.rideservice.dto;

public class UpdateDriverAvailabilityRequest {
    private String availabilityStatus;

    public UpdateDriverAvailabilityRequest() {}

    public UpdateDriverAvailabilityRequest(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
}
