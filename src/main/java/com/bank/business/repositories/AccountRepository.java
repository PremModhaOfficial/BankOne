package com.bank.business.repositories;

import com.bank.business.entities.Account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository
{
    Account save(Account account);

    Optional<Account> findById(Long id);

    List<Account> findByUserId(Long userId);

    List<Account> getAll();

    Optional<Account> findByAccountNumber(String accountNumber);

    void deleteById(Long id);
    // Add other necessary methods like findAll, update, etc.
}
