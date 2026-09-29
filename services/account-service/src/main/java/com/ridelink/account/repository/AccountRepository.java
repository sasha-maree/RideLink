package com.ridelink.account.repository;

import com.ridelink.account.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByAccountId(UUID accountId);
    boolean existsByEmail(String email);
}
