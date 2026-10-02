package com.ridelink.driver.service;

import com.ridelink.driver.dto.RegisterVehicleRequest;
import com.ridelink.driver.dto.VehicleResponse;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    public void testRegisterVehicle_Success() {
        UUID driverId = UUID.randomUUID();
        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setMake("Toyota");
        request.setModel("Corolla");
        request.setYear(2020);
        request.setPlateNumber("ABC-123");
        request.setCapacity(4);

        when(driverProfileRepository.existsById(driverId)).thenReturn(true);
        when(vehicleRepository.existsByPlateNumber("ABC-123")).thenReturn(false);
        when(vehicleRepository.findByDriverId(driverId)).thenReturn(Collections.emptyList());
        
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(UUID.randomUUID());
        vehicle.setDriverId(driverId);
        vehicle.setPlateNumber("ABC-123");
        vehicle.setIsActive(true);
        
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        
        DriverProfile profile = new DriverProfile();
        profile.setDriverId(driverId);
        when(driverProfileRepository.findById(driverId)).thenReturn(Optional.of(profile));

        VehicleResponse response = vehicleService.registerVehicle(driverId, request);

        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertEquals("ABC-123", response.getData().getPlateNumber());
    }

    @Test
    public void testRegisterVehicle_NoProfile() {
        UUID driverId = UUID.randomUUID();
        RegisterVehicleRequest request = new RegisterVehicleRequest();
        
        when(driverProfileRepository.existsById(driverId)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            vehicleService.registerVehicle(driverId, request);
        });
    }
}
