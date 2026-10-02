package com.ridelink.driver.service;

import com.ridelink.driver.dto.CreateDriverProfileRequest;
import com.ridelink.driver.dto.DriverProfileResponse;
import com.ridelink.driver.exception.ResourceConflictException;
import com.ridelink.driver.model.DriverProfile;
import com.ridelink.driver.repository.DriverProfileRepository;
import com.ridelink.driver.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriverProfileServiceTest {

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverProfileService driverProfileService;

    @Test
    public void testCreateProfile_Success() {
        UUID accountId = UUID.randomUUID();
        CreateDriverProfileRequest request = new CreateDriverProfileRequest();
        request.setLicenseNumber("LIC-5555");

        when(driverProfileRepository.existsById(accountId)).thenReturn(false);
        when(driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())).thenReturn(false);

        DriverProfile profile = new DriverProfile();
        profile.setDriverId(accountId);
        profile.setLicenseNumber("LIC-5555");
        
        when(driverProfileRepository.save(any(DriverProfile.class))).thenReturn(profile);

        DriverProfileResponse response = driverProfileService.createProfile(accountId, request);

        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertEquals("LIC-5555", response.getData().getLicenseNumber());
    }

    @Test
    public void testCreateProfile_ConflictExists() {
        UUID accountId = UUID.randomUUID();
        CreateDriverProfileRequest request = new CreateDriverProfileRequest();
        request.setLicenseNumber("LIC-5555");

        when(driverProfileRepository.existsById(accountId)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> {
            driverProfileService.createProfile(accountId, request);
        });
    }
}
