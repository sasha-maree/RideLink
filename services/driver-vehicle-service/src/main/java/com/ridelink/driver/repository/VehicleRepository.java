package com.ridelink.driver.repository;

import com.ridelink.driver.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, UUID> {

    boolean existsByPlateNumber(String plateNumber);

    List<Vehicle> findByDriverId(UUID driverId);
    
    Optional<Vehicle> findByDriverIdAndIsActiveTrue(UUID driverId);
}
