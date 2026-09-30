package com.ridelink.rideservice.controller;

import com.ridelink.rideservice.dto.*;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideHistoryEntry;
import com.ridelink.rideservice.service.RideService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    private UUID getAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            // For service tokens, principal is the service name string
            // We return a dummy UUID or null, but since it's used for checks, null might cause NPE.
            // checkAccess in RideService ignores accountId if role is SERVICE.
            return null;
        }
        return (UUID) principal;
    }

    private String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }

    private RideData convertToRideData(Ride ride) {
        RideData data = new RideData();
        data.setRideId(ride.getRideId());
        data.setPassengerId(ride.getPassengerId());
        data.setDriverId(ride.getDriverId());
        data.setVehicleId(ride.getVehicleId());
        data.setPickupLocation(ride.getPickupLocation());
        data.setDropoffLocation(ride.getDropoffLocation());
        data.setRideStatus(ride.getRideStatus());
        data.setEstimatedDistanceKm(ride.getEstimatedDistanceKm());
        data.setActualDistanceKm(ride.getActualDistanceKm());
        data.setDurationMinutes(ride.getDurationMinutes());
        data.setRequestedAt(ride.getRequestedAt());
        data.setAssignedAt(ride.getAssignedAt());
        data.setAcceptedAt(ride.getAcceptedAt());
        data.setStartedAt(ride.getStartedAt());
        data.setCompletedAt(ride.getCompletedAt());
        data.setCancelledAt(ride.getCancelledAt());
        return data;
    }

    private RideHistoryEntryDto convertToHistoryDto(RideHistoryEntry entry) {
        return new RideHistoryEntryDto(entry.getFromStatus(), entry.getToStatus(), entry.getChangedAt());
    }

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<RideResponse> requestRide(@Valid @RequestBody CreateRideRequest request) {
        Ride ride = rideService.requestRide(request, getAccountId());
        RideResponse response = new RideResponse(LocalDateTime.now(), HttpStatus.CREATED.value(),
                convertToRideData(ride));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<RideListResponse> listRides(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Ride> ridePage = rideService.listRides(getAccountId(), getRole(), status, page, size);

        List<RideData> items = ridePage.getContent().stream()
                .map(this::convertToRideData)
                .collect(Collectors.toList());

        RideListResponse response = new RideListResponse(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                items,
                (int) ridePage.getTotalElements(),
                page,
                size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable UUID rideId) {
        Ride ride = rideService.getRideById(rideId, getAccountId(), getRole());
        RideResponse response = new RideResponse(LocalDateTime.now(), HttpStatus.OK.value(), convertToRideData(ride));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable UUID rideId) {
        Ride ride = rideService.cancelRide(rideId, getAccountId(), getRole());
        RideResponse response = new RideResponse(LocalDateTime.now(), HttpStatus.OK.value(), convertToRideData(ride));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rideId}/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> updateRideStatus(
            @PathVariable UUID rideId,
            @Valid @RequestBody UpdateRideStatusRequest request) {

        Ride ride = rideService.updateRideStatus(rideId, request.getAction(), getAccountId());
        RideResponse response = new RideResponse(LocalDateTime.now(), HttpStatus.OK.value(), convertToRideData(ride));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{rideId}/history")
    public ResponseEntity<RideHistoryResponse> getRideHistory(@PathVariable UUID rideId) {
        Ride ride = rideService.getRideHistory(rideId, getAccountId(), getRole());

        List<RideHistoryEntryDto> historyDtos = ride.getHistory().stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());

        RideHistoryResponse response = new RideHistoryResponse(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                rideId,
                historyDtos);

        return ResponseEntity.ok(response);
    }
}
