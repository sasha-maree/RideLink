package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exception.AccountNotActiveException;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.repository.ProfileRepository;
import com.ridelink.account.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterPassenger_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("passenger@test.com");
        req.setPassword("password123");
        req.setRole(Role.PASSENGER);
        req.setFirstName("John");
        req.setLastName("Doe");

        when(accountRepository.existsByEmail("passenger@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        AccountResponse res = authService.registerAccount(req);

        assertNotNull(res);
        assertEquals(201, res.getStatus());
        assertEquals("passenger@test.com", res.getData().getEmail());
        assertEquals(Role.PASSENGER, res.getData().getRole());
        
        verify(accountRepository, times(1)).save(any());
        verify(profileRepository, times(1)).save(any());
    }

    @Test
    void testRegisterDriver_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("driver@test.com");
        req.setPassword("password123");
        req.setRole(Role.DRIVER);
        req.setFirstName("Jane");
        req.setLastName("Doe");

        when(accountRepository.existsByEmail("driver@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        AccountResponse res = authService.registerAccount(req);

        assertEquals(201, res.getStatus());
        assertEquals(Role.DRIVER, res.getData().getRole());
    }

    @Test
    void testRegister_DuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@test.com");
        
        when(accountRepository.existsByEmail("dup@test.com")).thenReturn(true);
        
        assertThrows(DuplicateEmailException.class, () -> authService.registerAccount(req));
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password123");

        Account acc = new Account(UUID.randomUUID(), "test@test.com", "hashed_password", Role.PASSENGER, AccountStatus.ACTIVE);
        when(accountRepository.findByEmail("test@test.com")).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateJwtToken(acc)).thenReturn("mock_token");

        LoginResponse res = authService.login(req);

        assertEquals(200, res.getStatus());
        assertEquals("mock_token", res.getData().getToken());
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("wrong");

        Account acc = new Account(UUID.randomUUID(), "test@test.com", "hashed_password", Role.PASSENGER, AccountStatus.ACTIVE);
        when(accountRepository.findByEmail("test@test.com")).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
    }
    
    @Test
    void testLogin_UnknownAccount() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        when(accountRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void testLogin_SuspendedAccount() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password123");

        Account acc = new Account(UUID.randomUUID(), "test@test.com", "hashed_password", Role.PASSENGER, AccountStatus.SUSPENDED);
        when(accountRepository.findByEmail("test@test.com")).thenReturn(Optional.of(acc));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);

        assertThrows(AccountNotActiveException.class, () -> authService.login(req));
    }
}
