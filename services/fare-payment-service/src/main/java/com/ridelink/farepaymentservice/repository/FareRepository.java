package com.ridelink.farepaymentservice.repository;

import com.ridelink.farepaymentservice.model.Fare;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FareRepository extends MongoRepository<Fare, String> {
    Optional<Fare> findByRideId(String rideId);
    Optional<Fare> findByFareId(String fareId);
}
