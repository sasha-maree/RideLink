package com.ridelink.rideservice.repository;

import com.ridelink.rideservice.model.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RideRepository extends MongoRepository<Ride, String> {
    Optional<Ride> findByRideId(UUID rideId);
    Page<Ride> findByPassengerId(UUID passengerId, Pageable pageable);
    Page<Ride> findByPassengerIdAndRideStatus(UUID passengerId, String rideStatus, Pageable pageable);
    Page<Ride> findByDriverId(UUID driverId, Pageable pageable);
    Page<Ride> findByDriverIdAndRideStatus(UUID driverId, String rideStatus, Pageable pageable);
    Page<Ride> findByRideStatus(String rideStatus, Pageable pageable);
}
