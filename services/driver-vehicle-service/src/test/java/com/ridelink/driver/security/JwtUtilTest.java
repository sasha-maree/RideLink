package com.ridelink.driver.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JwtUtil.class)
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testValidateJwtToken_Invalid() {
        boolean result = jwtUtil.validateJwtToken("invalid.token.here");
        assertFalse(result);
    }
}
