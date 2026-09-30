package com.ridelink.farepaymentservice.util;

import com.ridelink.farepaymentservice.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class OwnershipValidator {

    public static void validateRideOwnership(Map<String, Object> rideDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new UnauthorizedAccessException("Authentication required.");
        }
        
        String accountId = (String) auth.getPrincipal();
        String role = "";
        for (GrantedAuthority authority : auth.getAuthorities()) {
            role = authority.getAuthority();
        }

        if (role.equals("ROLE_ADMIN")) {
            return; // Admin has full access
        }

        if (role.equals("ROLE_PASSENGER")) {
            String passengerId = (String) rideDetails.get("passengerId");
            if (passengerId == null || !passengerId.equals(accountId)) {
                throw new UnauthorizedAccessException("You are not authorized to access this ride's details.");
            }
        } else if (role.equals("ROLE_DRIVER")) {
            String driverId = (String) rideDetails.get("driverId");
            if (driverId == null || !driverId.equals(accountId)) {
                throw new UnauthorizedAccessException("You are not authorized to access this ride's details.");
            }
        } else {
            throw new UnauthorizedAccessException("Unauthorized role.");
        }
    }
    
    public static void validatePassengerOwnership(Map<String, Object> rideDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new UnauthorizedAccessException("Authentication required.");
        }
        
        String accountId = (String) auth.getPrincipal();
        String role = "";
        for (GrantedAuthority authority : auth.getAuthorities()) {
            role = authority.getAuthority();
        }

        if (role.equals("ROLE_ADMIN")) {
            return; 
        }

        if (role.equals("ROLE_PASSENGER")) {
            String passengerId = (String) rideDetails.get("passengerId");
            if (passengerId == null || !passengerId.equals(accountId)) {
                throw new UnauthorizedAccessException("You are not authorized to access this ride's details.");
            }
        } else {
            throw new UnauthorizedAccessException("Only the ride's passenger may perform this action.");
        }
    }

    public static String getToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            return (String) auth.getCredentials();
        }
        return null;
    }
}
