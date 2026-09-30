package com.ridelink.account.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 6 hardened tests for Account Service JWT utility.
 * Tests user JWT validation and FARE_PAYMENT_SERVICE service JWT validation.
 */
@ExtendWith(MockitoExtension.class)
public class AccountJwtHardenedTest {

    private static final String USER_SECRET = "default-insecure-secret-for-local-dev-only";
    private static final String FARE_SERVICE_SECRET_B64 = "RmFyZVBheW1lbnRTZXJ2aWNlU3VwZXJTZWNyZXRLZXk=";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", USER_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "fareServiceSecret", FARE_SERVICE_SECRET_B64);
    }

    // ─── FARE_PAYMENT_SERVICE token validation ────────────────────────────────────

    @Test
    void validateFarePaymentServiceToken_validToken_returnsTrue() {
        String token = buildFarePaymentServiceToken(FARE_SERVICE_SECRET_B64, 60_000L);
        assertTrue(jwtUtil.validateFarePaymentServiceToken(token),
                "A correctly signed FARE_PAYMENT_SERVICE token must be accepted");
    }

    @Test
    void validateFarePaymentServiceToken_userJwt_returnsFalse() {
        // A normal user JWT must NOT pass the service token check
        String userToken = buildUserToken(USER_SECRET, "passenger-uuid", "PASSENGER", 60_000L);
        assertFalse(jwtUtil.validateFarePaymentServiceToken(userToken),
                "A user JWT must NOT be accepted as a FARE_PAYMENT_SERVICE token");
    }

    @Test
    void validateFarePaymentServiceToken_expiredToken_returnsFalse() {
        String expiredToken = buildFarePaymentServiceToken(FARE_SERVICE_SECRET_B64, -1_000L);
        assertFalse(jwtUtil.validateFarePaymentServiceToken(expiredToken),
                "An expired FARE_PAYMENT_SERVICE token must be rejected");
    }

    @Test
    void validateFarePaymentServiceToken_wrongSecret_returnsFalse() {
        String wrongKey = "a-completely-different-secret-key-32bytes!";
        String token = buildFarePaymentServiceToken(wrongKey, 60_000L);
        assertFalse(jwtUtil.validateFarePaymentServiceToken(token),
                "A token signed with the wrong key must be rejected");
    }

    @Test
    void validateFarePaymentServiceToken_missingClaims_returnsFalse() {
        // Token with correct key but missing type/service claims
        SecretKey key = Keys.hmacShaKeyFor(FARE_SERVICE_SECRET_B64.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("FARE_PAYMENT_SERVICE")
                // No type or service claims
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(key)
                .compact();

        assertFalse(jwtUtil.validateFarePaymentServiceToken(token),
                "A token missing type/service claims must be rejected");
    }

    // ─── User JWT validation ──────────────────────────────────────────────────────

    @Test
    void validateJwtToken_validUserToken_returnsTrue() {
        String token = buildUserToken(USER_SECRET, "passenger-uuid", "PASSENGER", 60_000L);
        assertTrue(jwtUtil.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        String token = buildUserToken(USER_SECRET, "passenger-uuid", "PASSENGER", -1_000L);
        assertFalse(jwtUtil.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_wrongSecret_returnsFalse() {
        String token = buildUserToken("wrong-secret-which-is-different-32chars", "uuid", "PASSENGER", 60_000L);
        assertFalse(jwtUtil.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_tampered_returnsFalse() {
        String token = buildUserToken(USER_SECRET, "uuid", "PASSENGER", 60_000L);
        // Tamper the signature
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtUtil.validateJwtToken(tampered));
    }

    @Test
    void getRoleFromJwtToken_extractsCorrectRole() {
        String token = buildUserToken(USER_SECRET, "driver-uuid", "DRIVER", 60_000L);
        assertEquals("DRIVER", jwtUtil.getRoleFromJwtToken(token));
    }

    @Test
    void getAccountIdFromJwtToken_extractsCorrectId() {
        String token = buildUserToken(USER_SECRET, "my-account-id", "PASSENGER", 60_000L);
        assertEquals("my-account-id", jwtUtil.getAccountIdFromJwtToken(token));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private String buildFarePaymentServiceToken(String secret, long expiryMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("FARE_PAYMENT_SERVICE")
                .claim("type", "SERVICE")
                .claim("service", "FARE_PAYMENT_SERVICE")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }

    private String buildUserToken(String secret, String subject, String role, long expiryMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
