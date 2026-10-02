package com.ridelink.driver.service;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.exception.ResourceConflictException;
import com.ridelink.driver.exception.ResourceNotFoundException;
import com.ridelink.driver.exception.ValidationException;
import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;

    public DriverProfileService(DriverProfileRepository driverProfileRepository, VehicleRepository vehicleRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public DriverProfileResponse createProfile(UUID accountId, CreateDriverProfileRequest request) {
        if (driverProfileRepository.existsById(accountId)) {
            throw new ResourceConflictException("Driver profile already exists for this account.");
        }
        if (driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new ResourceConflictException("License number is already registered.");
        }

        DriverProfile profile = new DriverProfile();
        profile.setDriverId(accountId);
        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        profile.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));

        DriverProfile saved = driverProfileRepository.save(profile);
        return new DriverProfileResponse(201, mapToData(saved));
    }

    public DriverProfileListResponse listAllDrivers(AvailabilityStatus status, int page, int size) {
        Page<DriverProfile> profilePage;
        if (status != null) {
            profilePage = driverProfileRepository.findByAvailabilityStatus(status, PageRequest.of(page - 1, size));
        } else {
            profilePage = driverProfileRepository.findAll(PageRequest.of(page - 1, size));
        }

        List<DriverProfileData> items = profilePage.getContent().stream()
                .map(this::mapToData)
                .collect(Collectors.toList());

        DriverProfileListResponse.DriverProfileListData data = new DriverProfileListResponse.DriverProfileListData(
                items,
                profilePage.getTotalElements(),
                page,
                size
        );
        return new DriverProfileListResponse(200, data);
    }

    public DriverProfileResponse getProfile(UUID driverId) {
        DriverProfile profile = getDriverProfileEntity(driverId);
        return new DriverProfileResponse(200, mapToData(profile));
    }

    public DriverProfileResponse updateProfile(UUID driverId, UpdateDriverProfileRequest request) {
        DriverProfile profile = getDriverProfileEntity(driverId);
        
        if (request.getLicenseNumber() != null && !request.getLicenseNumber().equals(profile.getLicenseNumber())) {
            if (driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
                throw new ResourceConflictException("License number is already registered.");
            }
            profile.setLicenseNumber(request.getLicenseNumber());
            profile.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        }

        DriverProfile updated = driverProfileRepository.save(profile);
        return new DriverProfileResponse(200, mapToData(updated));
    }

    public DriverProfileResponse setAvailability(UUID driverId, SetAvailabilityRequest request) {
        DriverProfile profile = getDriverProfileEntity(driverId);

        if (profile.getAvailabilityStatus() == AvailabilityStatus.ON_TRIP) {
            throw new ResourceConflictException("Cannot change availability while ON_TRIP.");
        }

        if (request.getAvailabilityStatus() == AvailabilityStatus.ON_TRIP) {
            throw new ValidationException("ON_TRIP cannot be set manually by the driver.");
        }

        if (request.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            Optional<Vehicle> activeVehicle = vehicleRepository.findByDriverIdAndIsActiveTrue(driverId);
            if (activeVehicle.isEmpty()) {
                throw new org.springframework.security.access.AccessDeniedException("No active vehicle – cannot go AVAILABLE.");
            }
            profile.setActiveVehicleId(activeVehicle.get().getVehicleId());
        } else if (request.getAvailabilityStatus() == AvailabilityStatus.OFFLINE) {
            profile.setActiveVehicleId(null);
        }

        profile.setAvailabilityStatus(request.getAvailabilityStatus());
        profile.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        DriverProfile updated = driverProfileRepository.save(profile);
        
        return new DriverProfileResponse(200, mapToData(updated));
    }

    public AvailableDriverListResponse getAvailableDrivers(int limit) {
        List<DriverProfile> availableDrivers = driverProfileRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE)
                .stream().limit(limit).toList();
        
        List<AvailableDriverSummary> summaries = availableDrivers.stream().map(driver -> {
            AvailableDriverSummary summary = new AvailableDriverSummary();
            summary.setDriverId(driver.getDriverId());
            summary.setVehicleId(driver.getActiveVehicleId());
            summary.setRating(driver.getRating());
            summary.setAvailabilityStatus(driver.getAvailabilityStatus());
            return summary;
        }).collect(Collectors.toList());
        
        return new AvailableDriverListResponse(200, summaries);
    }

    public DriverProfileResponse updateAvailability(UUID driverId, UpdateDriverAvailabilityRequest request) {
        DriverProfile profile = getDriverProfileEntity(driverId);
        
        profile.setAvailabilityStatus(request.getAvailabilityStatus());
        if (request.getAvailabilityStatus() == AvailabilityStatus.OFFLINE) {
            profile.setActiveVehicleId(null);
        } else if (request.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE && profile.getActiveVehicleId() == null) {
            Optional<Vehicle> activeVehicle = vehicleRepository.findByDriverIdAndIsActiveTrue(driverId);
            if (activeVehicle.isPresent()) {
                profile.setActiveVehicleId(activeVehicle.get().getVehicleId());
            } else {
                throw new ValidationException("Cannot set AVAILABLE without active vehicle.");
            }
        }
        profile.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));
        DriverProfile updated = driverProfileRepository.save(profile);
        
        return new DriverProfileResponse(200, mapToData(updated));
    }

    private DriverProfile getDriverProfileEntity(UUID driverId) {
        return driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found."));
    }

    private DriverProfileData mapToData(DriverProfile profile) {
        DriverProfileData data = new DriverProfileData();
        data.setDriverId(profile.getDriverId());
        data.setLicenseNumber(profile.getLicenseNumber());
        data.setRating(profile.getRating());
        data.setAvailabilityStatus(profile.getAvailabilityStatus());
        data.setActiveVehicleId(profile.getActiveVehicleId());
        data.setCreatedAt(profile.getCreatedAt());
        data.setUpdatedAt(profile.getUpdatedAt());
        return data;
    }
}
