package com.ridelink.account.service;

import com.ridelink.account.dto.*;
import com.ridelink.account.exception.AccountNotActiveException;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Profile;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.repository.ProfileRepository;
import com.ridelink.account.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AccountRepository accountRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AccountResponse registerAccount(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered");
        }

        UUID accountId = UUID.randomUUID();
        Account account = new Account(
                accountId,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                AccountStatus.ACTIVE
        );
        accountRepository.save(account);

        UUID profileId = UUID.randomUUID();
        Profile profile = new Profile(
                profileId,
                accountId,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber()
        );
        profileRepository.save(profile);

        AccountData data = new AccountData();
        data.setAccountId(accountId);
        data.setFirstName(profile.getFirstName());
        data.setLastName(profile.getLastName());
        data.setEmail(account.getEmail());
        data.setPhoneNumber(profile.getPhoneNumber());
        data.setRole(account.getRole());
        data.setAccountStatus(account.getStatus());
        data.setCreatedAt(account.getCreatedAt().toString());
        data.setUpdatedAt(account.getUpdatedAt().toString());

        return new AccountResponse(201, data);
    }

    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (account.getStatus() == AccountStatus.SUSPENDED || account.getStatus() == AccountStatus.DEACTIVATED) {
            throw new AccountNotActiveException("Account is suspended or deactivated");
        }

        String token = jwtUtil.generateJwtToken(account);
        LoginData data = new LoginData(token, account.getAccountId(), account.getRole());
        return new LoginResponse(200, data);
    }
}
