package com.ridelink.driver.controller;

import com.ridelink.driver.dto.RegisterVehicleRequest;
import com.ridelink.driver.dto.VehicleListResponse;
import com.ridelink.driver.dto.VehicleResponse;
import com.ridelink.driver.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> registerVehicle(
            @AuthenticationPrincipal UUID accountId,
            @Valid @RequestBody RegisterVehicleRequest request) {
        return new ResponseEntity<>(vehicleService.registerVehicle(accountId, request), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<VehicleListResponse> getMyVehicles(@AuthenticationPrincipal UUID accountId) {
        return ResponseEntity.ok(vehicleService.getMyVehicles(accountId));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId));
    }
}
