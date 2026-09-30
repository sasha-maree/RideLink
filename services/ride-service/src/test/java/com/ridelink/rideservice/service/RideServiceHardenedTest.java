package com.ridelink.rideservice.service;

import com.ridelink.rideservice.client.DriverVehicleClient;
import com.ridelink.rideservice.client.FareClient;
import com.ridelink.rideservice.dto.AvailableDriverSummary;
import com.ridelink.rideservice.exception.InvalidStatusTransitionException;
import com.ridelink.rideservice.exception.UnauthorizedException;
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

/**
 * Phase 6 hardened tests for N-02 (Invalid Ride Status Transition) and additional edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class RideServiceHardenedTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverVehicleClient driverVehicleClient;

    @Mock
    private FareClient fareClient;

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

    // ─── N-02: Invalid Status Transitions ───────────────────────────────────────

    @Test
    void n02_assignedToComplete_isInvalid() {
        Ride ride = rideInState(RideStatus.ASSIGNED);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.updateRideStatus(rideId, RideStatusAction.COMPLETE, driverId));

        assertTrue(ex.getMessage().contains("ASSIGNED") || ex.getMessage().contains("Cannot transition"));
        // Ride state must remain unchanged
        assertEquals(RideStatus.ASSIGNED, ride.getRideStatus());
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void n02_assignedToStart_isInvalid() {
        Ride ride = rideInState(RideStatus.ASSIGNED);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.updateRideStatus(rideId, RideStatusAction.START, driverId));
        assertEquals(RideStatus.ASSIGNED, ride.getRideStatus());
    }

    @Test
    void n02_requestedToComplete_isInvalid() {
        // REQUESTED state – only PASSENGER can have ride in REQUESTED (no driver assigned yet).
        // If somehow a driver tries to COMPLETE it, it must fail.
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setPassengerId(passengerId);
        ride.setDriverId(driverId);      // Driver IS set (edge case)
        ride.setRideStatus(RideStatus.REQUESTED);

        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.updateRideStatus(rideId, RideStatusAction.COMPLETE, driverId));
        assertEquals(RideStatus.REQUESTED, ride.getRideStatus());
    }

    @Test
    void n02_inProgressToAccept_isInvalid() {
        Ride ride = rideInState(RideStatus.IN_PROGRESS);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.updateRideStatus(rideId, RideStatusAction.ACCEPT, driverId));
        assertEquals(RideStatus.IN_PROGRESS, ride.getRideStatus());
    }

    @Test
    void n02_completedRide_cannotBeAcceptedAgain() {
        Ride ride = rideInState(RideStatus.COMPLETED);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.updateRideStatus(rideId, RideStatusAction.ACCEPT, driverId));
        assertEquals(RideStatus.COMPLETED, ride.getRideStatus());
    }

    @Test
    void n02_cancelledRide_cannotBeCancelled() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setPassengerId(passengerId);
        ride.setRideStatus(RideStatus.CANCELLED);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.cancelRide(rideId, passengerId, "PASSENGER"));
    }

    @Test
    void n02_completedRide_cannotBeCancelled() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setPassengerId(passengerId);
        ride.setRideStatus(RideStatus.COMPLETED);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.cancelRide(rideId, passengerId, "PASSENGER"));
    }

    // ─── Ownership / Authorization ───────────────────────────────────────────────

    @Test
    void wrongDriver_cannotUpdateStatus() {
        UUID wrongDriverId = UUID.randomUUID();
        Ride ride = rideInState(RideStatus.ASSIGNED);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(UnauthorizedException.class,
                () -> rideService.updateRideStatus(rideId, RideStatusAction.ACCEPT, wrongDriverId));
    }

    @Test
    void passenger_cannotCancelInProgressRide() {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setPassengerId(passengerId);
        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.cancelRide(rideId, passengerId, "PASSENGER"));
    }

    // ─── Fare client called on completion ────────────────────────────────────────

    @Test
    void completeRide_rideCompletion_setsFinalTimestamps() {
        Ride ride = rideInState(RideStatus.IN_PROGRESS);
        ride.setEstimatedDistanceKm(15.0);
        when(rideRepository.findByRideId(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Ride completed = rideService.updateRideStatus(rideId, RideStatusAction.COMPLETE, driverId);

        assertEquals(RideStatus.COMPLETED, completed.getRideStatus());
        assertNotNull(completed.getCompletedAt());
        // Actual distance must be populated
        assertNotNull(completed.getActualDistanceKm());
        assertTrue(completed.getActualDistanceKm() > 0);
        // Fare client must have been called
        verify(fareClient, times(1)).calculateFare(eq(rideId), any(Double.class), any(Integer.class));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    private Ride rideInState(RideStatus status) {
        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setPassengerId(passengerId);
        ride.setDriverId(driverId);
        ride.setRideStatus(status);
        return ride;
    }
}
