package com.ridelink.rideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "rides")
public class Ride {

    @Id
    private String id;
    
    @Field("rideId")
    private UUID rideId;
    
    @Field("passengerId")
    private UUID passengerId;
    
    @Field("driverId")
    private UUID driverId;
    
    @Field("vehicleId")
    private UUID vehicleId;
    
    private String pickupLocation;
    private String dropoffLocation;
    
    private RideStatus rideStatus;
    
    private Double estimatedDistanceKm;
    private Double actualDistanceKm;
    private Integer durationMinutes;
    
    private LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    
    private List<RideHistoryEntry> history = new ArrayList<>();

    public Ride() {
        this.rideId = UUID.randomUUID();
        this.requestedAt = LocalDateTime.now();
        this.rideStatus = RideStatus.REQUESTED;
        this.history.add(new RideHistoryEntry(null, RideStatus.REQUESTED, this.requestedAt));
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public UUID getRideId() { return rideId; }
    public void setRideId(UUID rideId) { this.rideId = rideId; }
    
    public UUID getPassengerId() { return passengerId; }
    public void setPassengerId(UUID passengerId) { this.passengerId = passengerId; }
    
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    
    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }
    
    public RideStatus getRideStatus() { return rideStatus; }
    public void setRideStatus(RideStatus rideStatus) { this.rideStatus = rideStatus; }
    
    public Double getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(Double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }
    
    public Double getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(Double actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public List<RideHistoryEntry> getHistory() { return history; }
    public void setHistory(List<RideHistoryEntry> history) { this.history = history; }
}
