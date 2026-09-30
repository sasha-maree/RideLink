package com.ridelink.account.security;

import com.ridelink.account.model.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret:default-insecure-secret-for-local-dev-only}")
    private String jwtSecret;

    @Value("${jwt.service.fare.secret:RmFyZVBheW1lbnRTZXJ2aWNlU3VwZXJTZWNyZXRLZXk=}")
    private String fareServiceSecret;

    // 8 hours in milliseconds
    private final int jwtExpirationMs = 8 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getFareServiceKey() {
        return Keys.hmacShaKeyFor(fareServiceSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(Account account) {
        return Jwts.builder()
                .subject(account.getAccountId().toString())
                .claim("role", account.getRole().name())
                .issuer("Account Service")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String getAccountIdFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    
    public String getRoleFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            // Invalid signature, expired, etc.
        }
        return false;
    }

    /**
     * Validates a service JWT issued by the Fare &amp; Payment Service.
     * Used to allow FPS to call GET /accounts/{id} for receipt generation.
     */
    public boolean validateFarePaymentServiceToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getFareServiceKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "SERVICE".equals(claims.get("type")) && "FARE_PAYMENT_SERVICE".equals(claims.get("service"));
        } catch (Exception e) {
            return false;
        }
    }
}
