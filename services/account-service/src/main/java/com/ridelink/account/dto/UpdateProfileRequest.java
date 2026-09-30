package com.ridelink.account.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[A-Za-z\\s\\-]+$")
    private String firstName;

    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[A-Za-z\\s\\-]+$")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9\\s\\-]{7,20}$")
    private String phoneNumber;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
