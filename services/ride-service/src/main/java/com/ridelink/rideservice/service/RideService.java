package com.ridelink.rideservice.service;

import com.ridelink.rideservice.client.DriverVehicleClient;
import com.ridelink.rideservice.dto.AvailableDriverSummary;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.exception.DriverUnavailableException;
import com.ridelink.rideservice.exception.InvalidStatusTransitionException;
import com.ridelink.rideservice.exception.ResourceNotFoundException;
import com.ridelink.rideservice.exception.UnauthorizedException;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideHistoryEntry;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.model.RideStatusAction;
import com.ridelink.rideservice.repository.RideRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverVehicleClient driverVehicleClient;
    private final com.ridelink.rideservice.client.FareClient fareClient;

    public RideService(RideRepository rideRepository, DriverVehicleClient driverVehicleClient, com.ridelink.rideservice.client.FareClient fareClient) {
        this.rideRepository = rideRepository;
        this.driverVehicleClient = driverVehicleClient;
        this.fareClient = fareClient;
    }

    @Transactional(noRollbackFor = DriverUnavailableException.class)
    public Ride requestRide(CreateRideRequest request, UUID passengerId) {
        Ride ride = new Ride();
        ride.setPassengerId(passengerId);
        ride.setPickupLocation(request.getPickupLocation());
        ride.setDropoffLocation(request.getDropoffLocation());
        ride.setEstimatedDistanceKm(request.getEstimatedDistanceKm());
        
        transitionStatus(ride, null, RideStatus.REQUESTED);
        ride.setRequestedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        try {
            List<AvailableDriverSummary> drivers = driverVehicleClient.getAvailableDrivers(1);
            if (drivers == null || drivers.isEmpty()) {
                transitionStatus(ride, RideStatus.REQUESTED, RideStatus.CANCELLED);
                ride.setCancelledAt(LocalDateTime.now());
                rideRepository.save(ride);
                throw new DriverUnavailableException("No drivers are currently available. Please try again later.");
            }
            
            AvailableDriverSummary selectedDriver = drivers.get(0);
            
            ride.setDriverId(selectedDriver.getDriverId());
            ride.setVehicleId(selectedDriver.getVehicleId());
            
            transitionStatus(ride, RideStatus.REQUESTED, RideStatus.ASSIGNED);
            ride.setAssignedAt(LocalDateTime.now());
            
            driverVehicleClient.updateDriverAvailability(selectedDriver.getDriverId(), "ON_TRIP");
            
            try {
                return rideRepository.save(ride);
            } catch (Exception e) {
                try {
                    driverVehicleClient.updateDriverAvailability(selectedDriver.getDriverId(), "AVAILABLE");
                } catch (Exception ex) {
                    // Log error but don't fail the compensation
                }
                throw e;
            }
        } catch (DriverUnavailableException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            transitionStatus(ride, RideStatus.REQUESTED, RideStatus.CANCELLED);
            ride.setCancelledAt(LocalDateTime.now());
            rideRepository.save(ride);
            throw new DriverUnavailableException("Could not assign a driver at this time. Cause: " + e.getMessage());
        }
    }

    public Page<Ride> listRides(UUID accountId, String role, String statusStr, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        if ("ADMIN".equals(role)) {
            if (statusStr != null) {
                return rideRepository.findByRideStatus(statusStr, pageable);
            }
            return rideRepository.findAll(pageable);
        } else if ("DRIVER".equals(role)) {
            if (statusStr != null) {
                return rideRepository.findByDriverIdAndRideStatus(accountId, statusStr, pageable);
            }
            return rideRepository.findByDriverId(accountId, pageable);
        } else if ("PASSENGER".equals(role)) {
            if (statusStr != null) {
                return rideRepository.findByPassengerIdAndRideStatus(accountId, statusStr, pageable);
            }
            return rideRepository.findByPassengerId(accountId, pageable);
        }
        
        throw new UnauthorizedException("Invalid role");
    }

    public Ride getRideById(UUID rideId, UUID accountId, String role) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
                
        checkAccess(ride, accountId, role);
        return ride;
    }
    
    public Ride getRideHistory(UUID rideId, UUID accountId, String role) {
        return getRideById(rideId, accountId, role);
    }

    @Transactional
    public Ride cancelRide(UUID rideId, UUID accountId, String role) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
                
        checkAccess(ride, accountId, role);
        
        RideStatus currentStatus = ride.getRideStatus();
        
        if (currentStatus == RideStatus.IN_PROGRESS || currentStatus == RideStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("A ride that is IN_PROGRESS or COMPLETED cannot be cancelled.");
        }
        if (currentStatus == RideStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Ride is already cancelled.");
        }
        
        if ("PASSENGER".equals(role) && (currentStatus == RideStatus.REQUESTED || currentStatus == RideStatus.ASSIGNED)) {
            // Valid passenger cancellation
        } else if ("DRIVER".equals(role) && currentStatus == RideStatus.ACCEPTED) {
            // Valid driver cancellation
        } else {
             throw new InvalidStatusTransitionException("You cannot cancel the ride at its current status.");
        }
        
        UUID assignedDriverId = ride.getDriverId();
        
        transitionStatus(ride, currentStatus, RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        
        ride = rideRepository.save(ride);
        
        if (assignedDriverId != null && currentStatus != RideStatus.REQUESTED) {
             try {
                 driverVehicleClient.updateDriverAvailability(assignedDriverId, "AVAILABLE");
             } catch (Exception e) {
                 // Log error but don't fail the cancellation
             }
        }
        
        return ride;
    }

    @Transactional
    public Ride updateRideStatus(UUID rideId, RideStatusAction action, UUID driverId) {
        Ride ride = rideRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
                
        if (ride.getDriverId() == null || !ride.getDriverId().equals(driverId)) {
            throw new UnauthorizedException("Not authorized to update this ride");
        }
        
        RideStatus currentStatus = ride.getRideStatus();
        RideStatus nextStatus;
        
        switch (action) {
            case ACCEPT:
                if (currentStatus != RideStatus.ASSIGNED) {
                    throw new InvalidStatusTransitionException("Cannot transition ride from " + currentStatus + " to ACCEPTED.");
                }
                nextStatus = RideStatus.ACCEPTED;
                ride.setAcceptedAt(LocalDateTime.now());
                break;
            case START:
                if (currentStatus != RideStatus.ACCEPTED) {
                     throw new InvalidStatusTransitionException("Cannot transition ride from " + currentStatus + " to IN_PROGRESS.");
                }
                nextStatus = RideStatus.IN_PROGRESS;
                ride.setStartedAt(LocalDateTime.now());
                break;
            case COMPLETE:
                if (currentStatus != RideStatus.IN_PROGRESS) {
                     throw new InvalidStatusTransitionException("Cannot transition ride from " + currentStatus + " to COMPLETED.");
                }
                nextStatus = RideStatus.COMPLETED;
                ride.setCompletedAt(LocalDateTime.now());
                ride.setActualDistanceKm(ride.getEstimatedDistanceKm() != null ? ride.getEstimatedDistanceKm() : 10.0);
                ride.setDurationMinutes(30);
                
                try {
                     driverVehicleClient.updateDriverAvailability(driverId, "AVAILABLE");
                } catch (Exception e) {
                     // Log error
                }
                // Fare Payment Service integration
                try {
                     fareClient.calculateFare(rideId, ride.getActualDistanceKm(), ride.getDurationMinutes());
                } catch (Exception e) {
                     e.printStackTrace();
                     // Log error but proceed with completion locally
                }
                break;
            default:
                throw new InvalidStatusTransitionException("Invalid action.");
        }
        
        transitionStatus(ride, currentStatus, nextStatus);
        
        return rideRepository.save(ride);
    }
    
    private void checkAccess(Ride ride, UUID accountId, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if ("PASSENGER".equals(role) && !accountId.equals(ride.getPassengerId())) {
             throw new UnauthorizedException("Forbidden – not authorized to view this ride");
        }
        if ("DRIVER".equals(role) && !accountId.equals(ride.getDriverId())) {
             throw new UnauthorizedException("Forbidden – not authorized to view this ride");
        }
    }
    
    private void transitionStatus(Ride ride, RideStatus from, RideStatus to) {
        ride.setRideStatus(to);
        ride.getHistory().add(new RideHistoryEntry(from, to, LocalDateTime.now()));
    }
}
