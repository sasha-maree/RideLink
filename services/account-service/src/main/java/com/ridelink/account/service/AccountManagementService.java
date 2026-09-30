package com.ridelink.account.service;

import com.ridelink.account.dto.*;
import com.ridelink.account.exception.ResourceNotFoundException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Profile;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountManagementService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    public AccountManagementService(AccountRepository accountRepository, ProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    public AccountResponse getAccountById(UUID accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        return new AccountResponse(200, mapToAccountData(account, profile));
    }

    public AccountResponse updateMyAccount(UUID accountId, UpdateProfileRequest request) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        boolean isUpdated = false;
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
            isUpdated = true;
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
            isUpdated = true;
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
            isUpdated = true;
        }

        if (isUpdated) {
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
            
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }

        return new AccountResponse(200, mapToAccountData(account, profile));
    }

    public AccountListResponse listAccounts(Role role, AccountStatus status, int page, int size) {
        List<Account> allAccounts = accountRepository.findAll();
        
        if (role != null) {
            allAccounts = allAccounts.stream().filter(a -> a.getRole() == role).collect(Collectors.toList());
        }
        if (status != null) {
            allAccounts = allAccounts.stream().filter(a -> a.getStatus() == status).collect(Collectors.toList());
        }
        
        long total = allAccounts.size();
        
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allAccounts.size());
        
        List<Account> pageAccounts = fromIndex < allAccounts.size() ? allAccounts.subList(fromIndex, toIndex) : List.of();
        
        List<AccountData> items = pageAccounts.stream().map(account -> {
            Profile profile = profileRepository.findByAccountId(account.getAccountId()).orElse(new Profile());
            return mapToAccountData(account, profile);
        }).collect(Collectors.toList());
        
        AccountListResponse.AccountListData data = new AccountListResponse.AccountListData(items, total, page, size);
        return new AccountListResponse(200, data);
    }

    public AccountResponse updateAccountStatus(UUID accountId, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                
        account.setStatus(request.getStatus());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        
        Profile profile = profileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
                
        return new AccountResponse(200, mapToAccountData(account, profile));
    }
    
    private AccountData mapToAccountData(Account account, Profile profile) {
        AccountData data = new AccountData();
        data.setAccountId(account.getAccountId());
        data.setFirstName(profile.getFirstName());
        data.setLastName(profile.getLastName());
        data.setEmail(account.getEmail());
        data.setPhoneNumber(profile.getPhoneNumber());
        data.setRole(account.getRole());
        data.setAccountStatus(account.getStatus());
        data.setCreatedAt(account.getCreatedAt().toString());
        data.setUpdatedAt(account.getUpdatedAt().toString());
        return data;
    }
}
