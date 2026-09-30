package com.ridelink.driver.service;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.exception.ResourceConflictException;
import com.ridelink.driver.exception.ResourceNotFoundException;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverProfileRepository driverProfileRepository;

    public VehicleService(VehicleRepository vehicleRepository, DriverProfileRepository driverProfileRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    public VehicleResponse registerVehicle(UUID driverId, RegisterVehicleRequest request) {
        if (!driverProfileRepository.existsById(driverId)) {
            throw new org.springframework.security.access.AccessDeniedException("Driver profile must exist before registering a vehicle.");
        }
        
        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new ResourceConflictException("Plate number already registered.");
        }

        // Deactivate other vehicles for this driver
        List<Vehicle> existingVehicles = vehicleRepository.findByDriverId(driverId);
        for (Vehicle v : existingVehicles) {
            if (Boolean.TRUE.equals(v.getIsActive())) {
                v.setIsActive(false);
                vehicleRepository.save(v);
            }
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setDriverId(driverId);
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setCapacity(request.getCapacity());
        vehicle.setColor(request.getColor());
        vehicle.setIsActive(true); // new vehicle becomes active
        vehicle.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));

        Vehicle saved = vehicleRepository.save(vehicle);
        
        // Update driver profile with active vehicle
        DriverProfile profile = driverProfileRepository.findById(driverId).orElse(null);
        if (profile != null) {
            profile.setActiveVehicleId(saved.getVehicleId());
            driverProfileRepository.save(profile);
        }

        return new VehicleResponse(201, mapToData(saved));
    }

    public VehicleListResponse getMyVehicles(UUID driverId) {
        List<VehicleData> dataList = vehicleRepository.findByDriverId(driverId)
                .stream()
                .map(this::mapToData)
                .collect(Collectors.toList());
        return new VehicleListResponse(200, dataList);
    }

    public VehicleResponse getVehicleById(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found."));
        return new VehicleResponse(200, mapToData(vehicle));
    }

    private VehicleData mapToData(Vehicle vehicle) {
        VehicleData data = new VehicleData();
        data.setVehicleId(vehicle.getVehicleId());
        data.setDriverId(vehicle.getDriverId());
        data.setMake(vehicle.getMake());
        data.setModel(vehicle.getModel());
        data.setYear(vehicle.getYear());
        data.setPlateNumber(vehicle.getPlateNumber());
        data.setCapacity(vehicle.getCapacity());
        data.setColor(vehicle.getColor());
        data.setIsActive(vehicle.getIsActive());
        data.setCreatedAt(vehicle.getCreatedAt());
        return data;
    }
}
