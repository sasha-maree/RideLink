package com.ridelink.driver.controller;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.service.DriverProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverProfileController {

    private final DriverProfileService driverProfileService;

    public DriverProfileController(DriverProfileService driverProfileService) {
        this.driverProfileService = driverProfileService;
    }

    @PostMapping
    public ResponseEntity<DriverProfileResponse> createDriverProfile(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody CreateDriverProfileRequest request) {
        return new ResponseEntity<>(driverProfileService.createProfile(accountId, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<DriverProfileListResponse> listAllDrivers(
            @RequestParam(required = false) AvailabilityStatus availability,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(driverProfileService.listAllDrivers(availability, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<DriverProfileResponse> getMyDriverProfile(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(driverProfileService.getProfile(accountId));
    }

    @PatchMapping("/me")
    public ResponseEntity<DriverProfileResponse> updateMyDriverProfile(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody UpdateDriverProfileRequest request) {
        return ResponseEntity.ok(driverProfileService.updateProfile(accountId, request));
    }

    @PatchMapping("/me/availability")
    public ResponseEntity<DriverProfileResponse> setMyAvailability(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody SetAvailabilityRequest request) {
        return ResponseEntity.ok(driverProfileService.setAvailability(accountId, request));
    }

    @GetMapping("/available")
    public ResponseEntity<AvailableDriverListResponse> getAvailableDrivers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(driverProfileService.getAvailableDrivers(limit));
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverProfileResponse> getDriverById(@PathVariable UUID driverId) {
        return ResponseEntity.ok(driverProfileService.getProfile(driverId));
    }

    @PatchMapping("/{driverId}/availability")
    public ResponseEntity<DriverProfileResponse> updateDriverAvailability(
            @PathVariable UUID driverId,
            @Valid @RequestBody UpdateDriverAvailabilityRequest request) {
        return ResponseEntity.ok(driverProfileService.updateAvailability(driverId, request));
    }
}
