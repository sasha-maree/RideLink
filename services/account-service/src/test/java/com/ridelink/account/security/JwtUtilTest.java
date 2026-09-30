package com.ridelink.account.security;

import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set a long dummy secret for test
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "ThisIsAVeryLongSecretKeyUsedForTestingPurposesOnly");
    }

    @Test
    void testGenerateAndValidateToken() {
        Account acc = new Account(UUID.randomUUID(), "test@test.com", "hash", Role.DRIVER, AccountStatus.ACTIVE);
        
        String token = jwtUtil.generateJwtToken(acc);
        assertNotNull(token);
        
        assertTrue(jwtUtil.validateJwtToken(token));
        
        assertEquals(acc.getAccountId().toString(), jwtUtil.getAccountIdFromJwtToken(token));
        assertEquals(Role.DRIVER.name(), jwtUtil.getRoleFromJwtToken(token));
    }
    
    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.validateJwtToken("invalid.token.string"));
    }
}
