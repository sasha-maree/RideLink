package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.client.RideClient;
import com.ridelink.farepaymentservice.dto.FareData;
import com.ridelink.farepaymentservice.dto.FareEstimateData;
import com.ridelink.farepaymentservice.exception.FareAlreadyCalculatedException;
import com.ridelink.farepaymentservice.exception.ResourceNotFoundException;
import com.ridelink.farepaymentservice.exception.RideNotCompletedException;
import com.ridelink.farepaymentservice.model.Fare;
import com.ridelink.farepaymentservice.repository.FareRepository;
import com.ridelink.farepaymentservice.util.OwnershipValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class FareService {

    private final FareRepository fareRepository;
    private final RideClient rideClient;

    private static final BigDecimal BASE_FARE = new BigDecimal("300.00");
    private static final BigDecimal RATE_PER_KM = new BigDecimal("100.00");
    private static final String CURRENCY = "LKR";

    public FareService(FareRepository fareRepository, RideClient rideClient) {
        this.fareRepository = fareRepository;
        this.rideClient = rideClient;
    }

    public FareEstimateData estimateFare(Double estimatedDistanceKm) {
        BigDecimal distance = BigDecimal.valueOf(estimatedDistanceKm);
        BigDecimal distanceFare = distance.multiply(RATE_PER_KM).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalFare = BASE_FARE.add(distanceFare);

        FareEstimateData data = new FareEstimateData();
        data.setEstimatedDistanceKm(estimatedDistanceKm);
        data.setBaseFare(BASE_FARE);
        data.setDistanceFare(distanceFare);
        data.setSurcharge(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        data.setEstimatedTotal(totalFare);
        data.setCurrency(CURRENCY);

        return data;
    }

    public FareData calculateFare(String rideId, Double actualDistanceKm, Integer durationMinutes) {
        if (fareRepository.findByRideId(rideId).isPresent()) {
            throw new FareAlreadyCalculatedException("A fare has already been calculated for this ride.");
        }

        // Verify ride is completed
        Map<String, Object> rideDetails = rideClient.getRideDetails(rideId);
        String status = (String) rideDetails.get("status");
        if (!"COMPLETED".equals(status)) {
            throw new RideNotCompletedException("Ride is not completed yet.");
        }

        BigDecimal distance = BigDecimal.valueOf(actualDistanceKm);
        BigDecimal distanceFare = distance.multiply(RATE_PER_KM).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalFare = BASE_FARE.add(distanceFare);

        Fare fare = new Fare();
        fare.setFareId(UUID.randomUUID().toString());
        fare.setRideId(rideId);
        fare.setBaseFare(BASE_FARE);
        fare.setDistanceFare(distanceFare);
        fare.setSurcharge(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        fare.setTotalAmount(totalFare);
        fare.setCurrency(CURRENCY);
        fare.setCalculatedAt(LocalDateTime.now());

        Fare savedFare = fareRepository.save(fare);
        return mapToFareData(savedFare);
    }

    public FareData getFareByRideId(String rideId) {
        OwnershipValidator.validateRideOwnership(rideClient.getRideDetails(rideId));
        Fare fare = fareRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found for ride: " + rideId));
        return mapToFareData(fare);
    }

    private FareData mapToFareData(Fare fare) {
        FareData data = new FareData();
        data.setFareId(fare.getFareId());
        data.setRideId(fare.getRideId());
        data.setBaseFare(fare.getBaseFare());
        data.setDistanceFare(fare.getDistanceFare());
        data.setSurcharge(fare.getSurcharge());
        data.setTotalAmount(fare.getTotalAmount());
        data.setCurrency(fare.getCurrency());
        data.setCalculatedAt(fare.getCalculatedAt());
        return data;
    }
}
