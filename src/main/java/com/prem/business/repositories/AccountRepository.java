package com.prem.business.repositories;

import com.prem.business.entities.Account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    void deleteById(Long id);
    // Add other necessary methods like findAll, update, etc.
}