package com.ridelink.farepaymentservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final SecretKey userKey;
    private final SecretKey rideServiceKey;
    private final SecretKey farePaymentServiceKey;

    public JwtUtil(
            @Value("${jwt.secret:default-insecure-secret-for-local-dev-only}") String secret,
            @Value("${jwt.service.ride.secret:UmlkZVNlcnZpY2VTdXBlclNlY3JldEtleTIwMjQhQCMk}") String rideSecret,
            @Value("${jwt.service.fare.secret:RmFyZVBheW1lbnRTZXJ2aWNlU3VwZXJTZWNyZXRLZXk=}") String fareSecret) {
        this.userKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.rideServiceKey = Keys.hmacShaKeyFor(rideSecret.getBytes(StandardCharsets.UTF_8));
        this.farePaymentServiceKey = Keys.hmacShaKeyFor(fareSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUserId(String token) {
        return extractAllUserClaims(token).getSubject();
    }
    
    public String extractRole(String token) {
        return extractAllUserClaims(token).get("role", String.class);
    }
    
    public boolean validateUserToken(String token) {
        try {
            extractAllUserClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Claims extractAllUserClaims(String token) {
        return Jwts.parser()
                .verifyWith(userKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public Claims extractAllRideServiceClaims(String token) {
        return Jwts.parser()
                .verifyWith(rideServiceKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateRideServiceToken(String token) {
        try {
            Claims claims = extractAllRideServiceClaims(token);
            return "SERVICE".equals(claims.get("type")) && "RIDE_SERVICE".equals(claims.get("service"));
        } catch (Exception e) {
            return false;
        }
    }

    public String generateFarePaymentServiceToken() {
        return Jwts.builder()
                .subject("FARE_PAYMENT_SERVICE")
                .claim("type", "SERVICE")
                .claim("service", "FARE_PAYMENT_SERVICE")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60000)) // 1 minute expiration
                .signWith(farePaymentServiceKey)
                .compact();
    }
}
