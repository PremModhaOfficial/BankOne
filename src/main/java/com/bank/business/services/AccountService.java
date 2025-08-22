package com.bank.business.services;

import com.bank.business.entities.Account;
import com.bank.business.repositories.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Long userId, BigDecimal initialBalance, Account.AccountType type) {
        // Create account without explicit account number - it will be generated
        Account account = new Account(userId, initialBalance, type);
        return accountRepository.save(account);
    }

    // Overloaded method for backward compatibility
    public Account createAccount(Long userId, String accountNumber, BigDecimal initialBalance, Account.AccountType type) {
        // Add validation logic here (e.g., check if user exists, balance is non-negative)
        Account account = new Account(userId, accountNumber, initialBalance, type);
        Account savedAccount = accountRepository.save(account);
        
        // Ensure account number is generated if not provided
        if (savedAccount.getAccountNumber() == null) {
            savedAccount.setAccountNumber("ACC" + String.format("%06d", savedAccount.getId()));
            // Update the account with the generated number
            return accountRepository.save(savedAccount);
        }
        
        return savedAccount;
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
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
