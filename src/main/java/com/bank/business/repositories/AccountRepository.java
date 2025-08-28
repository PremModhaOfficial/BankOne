package com.bank.business.repositories;

import com.bank.business.entities.Account;
import java.util.List;

public interface AccountRepository
{
    Account save(Account account);

    Account findById(Long id);

    List<Account> findByUserId(Long userId);

    List<Account> getAll();

    Account findByAccountNumber(String accountNumber);

    void deleteById(Long id);
    // Add other necessary methods like findAll, update, etc.
}
