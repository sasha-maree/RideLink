package com.ridelink.farepaymentservice.controller;

import com.ridelink.farepaymentservice.dto.CalculateFareRequest;
import com.ridelink.farepaymentservice.dto.FareEstimateRequest;
import com.ridelink.farepaymentservice.dto.FareEstimateResponse;
import com.ridelink.farepaymentservice.dto.FareResponse;
import com.ridelink.farepaymentservice.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        return ResponseEntity.ok(new FareEstimateResponse(200, fareService.estimateFare(request.getEstimatedDistanceKm())));
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('SERVICE') and principal == 'RIDE_SERVICE'")
    public ResponseEntity<FareResponse> calculateFare(@Valid @RequestBody CalculateFareRequest request) {
        return ResponseEntity.ok(new FareResponse(200, fareService.calculateFare(request.getRideId(), request.getActualDistanceKm(), request.getDurationMinutes())));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<FareResponse> getFareByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(new FareResponse(200, fareService.getFareByRideId(rideId)));
    }
}
