package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.UpdateAccountStatusRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Profile;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountManagementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private AccountManagementService accountManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAccountById() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, "test@test.com", "hash", Role.PASSENGER, AccountStatus.ACTIVE);
        Profile profile = new Profile(UUID.randomUUID(), accountId, "John", "Doe", "+123456789");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        AccountResponse res = accountManagementService.getAccountById(accountId);

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        assertEquals("John", res.getData().getFirstName());
        assertEquals(accountId, res.getData().getAccountId());
    }

    @Test
    void testUpdateMyAccount() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, "test@test.com", "hash", Role.PASSENGER, AccountStatus.ACTIVE);
        Profile profile = new Profile(UUID.randomUUID(), accountId, "John", "Doe", "+123456789");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("Jane");

        AccountResponse res = accountManagementService.updateMyAccount(accountId, req);

        assertEquals(200, res.getStatus());
        assertEquals("Jane", res.getData().getFirstName());
        assertEquals("Doe", res.getData().getLastName());
        
        verify(profileRepository, times(1)).save(any());
        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void testUpdateAccountStatus() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, "test@test.com", "hash", Role.PASSENGER, AccountStatus.ACTIVE);
        Profile profile = new Profile(UUID.randomUUID(), accountId, "John", "Doe", "+123456789");

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));
        when(profileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        UpdateAccountStatusRequest req = new UpdateAccountStatusRequest();
        req.setStatus(AccountStatus.SUSPENDED);

        AccountResponse res = accountManagementService.updateAccountStatus(accountId, req);

        assertEquals(200, res.getStatus());
        assertEquals(AccountStatus.SUSPENDED, res.getData().getAccountStatus());
        
        verify(accountRepository, times(1)).save(any());
    }
}
