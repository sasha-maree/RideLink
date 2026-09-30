package com.ridelink.rideservice.service;

import com.ridelink.rideservice.client.DriverVehicleClient;
import com.ridelink.rideservice.dto.AvailableDriverSummary;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.exception.DriverUnavailableException;
import com.ridelink.rideservice.exception.InvalidStatusTransitionException;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.model.RideStatusAction;
import com.ridelink.rideservice.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverVehicleClient driverVehicleClient;

    @Mock
    private com.ridelink.rideservice.client.FareClient fareClient;

    @InjectMocks
    private RideService rideService;

    private UUID passengerId;
    private UUID driverId;
    private UUID rideId;

    @BeforeEach
    void setUp() {
        passengerId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        rideId = UUID.randomUUID();
    }

    @Test
    void testRequestRide_Success() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickupLocation("A");
        request.setDropoffLocation("B");
        request.setEstimatedDistanceKm(5.0);

        Ride savedRide = new Ride();
        savedRide.setRideId(rideId);
        savedRide.setPassengerId(passengerId);
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);

        AvailableDriverSummary driverSummary = new AvailableDriverSummary();
        driverSummary.setDriverId(driverId);
        when(driverVehicleClient.getAvailableDrivers(1)).thenReturn(Collections.singletonList(driverSummary));

        Ride ride = rideService.requestRide(request, passengerId);

        assertNotNull(ride);
        verify(driverVehicleClient, times(1)).updateDriverAvailability(driverId, "ON_TRIP");
    }

    @Test
    void testRequestRide_NoDriverAvailable() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickupLocation("A");
        request.setDropoffLocation("B");

        Ride savedRide = new Ride();
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        when(driverVehicleClient.getAvailableDrivers(1)).thenReturn(Collections.emptyList());

        assertThrows(DriverUnavailableException.class, () -> rideService.requestRide(request, passengerId));
        
        org.mockito.ArgumentCaptor<Ride> rideCaptor = org.mockito.ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository, atLeast(2)).save(rideCaptor.capture());
        assertEquals(RideStatus.CANCELLED, rideCaptor.getValue().getRideStatus());
    }

    @Test
    void testCancelRide_DriverCancelAssigned_Rejected() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.ASSIGNED);

        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class, () -> rideService.cancelRide(rideId, driverId, "DRIVER"));
    }

    @Test
    void testRequestRide_DvsOnTripSucceedsButRideSaveFails() {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickupLocation("A");
        request.setDropoffLocation("B");

        Ride savedRide = new Ride();
        savedRide.setRideId(rideId);
        savedRide.setPassengerId(passengerId);

        when(rideRepository.save(any(Ride.class)))
            .thenReturn(savedRide)
            .thenThrow(new RuntimeException("Database timeout"));

        AvailableDriverSummary driverSummary = new AvailableDriverSummary();
        driverSummary.setDriverId(driverId);
        when(driverVehicleClient.getAvailableDrivers(1)).thenReturn(Collections.singletonList(driverSummary));

        assertThrows(RuntimeException.class, () -> rideService.requestRide(request, passengerId));

        verify(driverVehicleClient, times(1)).updateDriverAvailability(driverId, "ON_TRIP");
        verify(driverVehicleClient, times(1)).updateDriverAvailability(driverId, "AVAILABLE");
    }

    @Test
    void testUpdateRideStatus_InvalidTransition() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.ASSIGNED);

        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        // Assigned to Complete is invalid
        assertThrows(InvalidStatusTransitionException.class, () -> rideService.updateRideStatus(rideId, RideStatusAction.COMPLETE, driverId));
    }

    @Test
    void testCancelRide_DriverCancelAccepted() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.ACCEPTED);

        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        Ride cancelledRide = rideService.cancelRide(rideId, driverId, "DRIVER");

        assertEquals(RideStatus.CANCELLED, cancelledRide.getRideStatus());
        verify(driverVehicleClient, times(1)).updateDriverAvailability(driverId, "AVAILABLE");
    }

    @Test
    void testUpdateRideStatus_Complete_CallsFareClient() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setEstimatedDistanceKm(10.0);
        
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));
        
        Ride completedRide = rideService.updateRideStatus(rideId, RideStatusAction.COMPLETE, driverId);
        
        assertEquals(RideStatus.COMPLETED, completedRide.getRideStatus());
        verify(fareClient, times(1)).calculateFare(rideId, 10.0, 30);
    }
}
