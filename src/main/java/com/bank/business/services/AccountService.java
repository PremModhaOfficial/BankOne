package com.bank.business.services;

import com.bank.business.entities.Account;
import com.bank.business.repositories.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService
{
    private final AccountRepository accountRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepository accountRepository)
    {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Long userId, BigDecimal initialBalance, Account.AccountType type)
    {
        // Create account without explicit account number - it will be generated

        Account account = new Account(userId, initialBalance, type);
        LOGGER.debug("Created Account {}", account);
        return accountRepository.save(account);
    }

    // Overloaded method for backward compatibility
    public Account createAccount(Long userId, String accountNumber, BigDecimal initialBalance, Account.AccountType type)
    {
        Account account = new Account(userId, accountNumber, initialBalance, type);
        Account savedAccount = accountRepository.save(account);

        // Ensure account number is generated if not provided
        if (savedAccount.getAccountNumber() == null)
        {
            savedAccount.setAccountNumber("ACC" + String.format("%06d", savedAccount.getId()));
            // Update the account with the generated number
            return accountRepository.save(savedAccount);
        }

        return savedAccount;
    }

    public Account getAccountById(Long id)
    {
        return accountRepository.findById(id);
    }

    public List<Account> getAccountsByUserId(Long userId)
    {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccountByAccountNumber(String accountNumber)
    {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public Account updateAccount(Account account)
    {
        // Add validation logic here if needed
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id)
    {
        accountRepository.deleteById(id);
    }

    public List<Account> getAllAccounts()
    {
        return accountRepository.getAll();
    }

    /**
     * Transfers amount from one account to another atomically.
     * 
     * @param fromAccountId The ID of the account to transfer from
     * @param toAccountId   The ID of the account to transfer to
     * @param amount        The amount to transfer
     * @return true if the transfer was successful, false otherwise
     */
    public boolean transferAmount(Long fromAccountId, Long toAccountId, BigDecimal amount)
    {
        // Get both accounts
        Account fromAccount = getAccountById(fromAccountId);
        Account toAccount = getAccountById(toAccountId);

        if (fromAccount == null || toAccount == null)
        {
            return false;
        }

        // To ensure atomicity of the transfer operation, we need to synchronize on both
        // accounts
        // We'll use a consistent locking order to prevent deadlocks
        var firstWriteLock = (fromAccount.getId() < toAccount.getId() ? fromAccount : toAccount).getReadWriteLock().readLock();
        var secondWriteLock = (fromAccount.getId() < toAccount.getId() ? toAccount : fromAccount).getReadWriteLock().readLock();

        if (firstWriteLock.tryLock())
        {
            if (secondWriteLock.tryLock())
            {
                // Perform the transfer atomically
                boolean success = fromAccount.withdrawAmount(amount);
                if (success)
                {
                    toAccount.addAmount(amount);
                    // Update both accounts in the repository
                    updateAccount(fromAccount);
                    updateAccount(toAccount);
                }

                firstWriteLock.unlock();
                secondWriteLock.unlock();
                return success;
            }
        }
        return false;

    }
}
