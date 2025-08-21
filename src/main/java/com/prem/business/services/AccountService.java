package com.prem.business.services;

import com.prem.business.entities.Account;
import com.prem.business.repositories.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Long userId, String accountNumber, BigDecimal initialBalance, Account.AccountType type) {
        // Add validation logic here (e.g., check if user exists, balance is non-negative)
        Account account = new Account(userId, accountNumber, initialBalance, type);
        return accountRepository.save(account);
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Account updateAccount(Account account) {
        // Add validation logic here if needed
        account.setUpdatedAt(java.time.LocalDateTime.now());
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}