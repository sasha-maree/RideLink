package com.ridelink.driver.repository;

import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.DriverProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverProfileRepository extends MongoRepository<DriverProfile, UUID> {
    
    boolean existsByLicenseNumber(String licenseNumber);

    Page<DriverProfile> findByAvailabilityStatus(AvailabilityStatus availabilityStatus, Pageable pageable);

    List<DriverProfile> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);
}
