package com.bank.infrastructure.persistence.database;

import com.bank.business.entities.Account;
import com.bank.business.repositories.AccountRepository;
import java.util.List;
import java.util.Optional;

// Placeholder for a future database implementation.
// This class won't compile until you add actual database logic (e.g., JDBC, JPA).
public class DatabaseAccountRepository implements AccountRepository {

    @Override
    public Account save(Account account) {
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public Optional<Account> findById(Long id) {
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public List<Account> findByUserId(Long userId) {
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public List<Account> getAll() {
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }
}
