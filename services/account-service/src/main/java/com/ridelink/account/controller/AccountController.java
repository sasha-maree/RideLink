package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountListResponse;
import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.UpdateAccountStatusRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.service.AccountManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountManagementService accountManagementService;

    public AccountController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMyAccount(Authentication authentication) {
        UUID accountId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(accountManagementService.getAccountById(accountId));
    }

    @PatchMapping("/me")
    public ResponseEntity<AccountResponse> updateMyAccount(Authentication authentication,
                                                           @Valid @RequestBody UpdateProfileRequest request) {
        UUID accountId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(accountManagementService.updateMyAccount(accountId, request));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE') or principal == #accountId")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountManagementService.getAccountById(accountId));
    }

    @GetMapping
    public ResponseEntity<AccountListResponse> listAccounts(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(accountManagementService.listAccounts(role, status, page, size));
    }

    @PatchMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable UUID accountId,
                                                               @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ResponseEntity.ok(accountManagementService.updateAccountStatus(accountId, request));
    }
}
