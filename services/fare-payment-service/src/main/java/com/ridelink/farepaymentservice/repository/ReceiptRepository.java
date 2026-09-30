package com.ridelink.farepaymentservice.repository;

import com.ridelink.farepaymentservice.model.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReceiptRepository extends MongoRepository<Receipt, String> {
    Optional<Receipt> findByRideId(String rideId);
    Optional<Receipt> findByReceiptId(String receiptId);
}
