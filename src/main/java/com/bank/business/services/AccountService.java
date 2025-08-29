package com.bank.business.services;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.entities.Account;
import com.bank.business.repositories.AccountRepository;

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

        var account = new Account(userId, initialBalance, type);
        LOGGER.debug("Created Account {}", account);
        return accountRepository.save(account);
    }

    // Overloaded method for backward compatibility
    public Account createAccount(Long userId, String accountNumber, BigDecimal initialBalance, Account.AccountType type)
    {
        var account = new Account(userId, accountNumber, initialBalance, type);
        var savedAccount = accountRepository.save(account);

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
        // Validate input parameters
        if (fromAccountId == null || toAccountId == null || amount == null)
        {
            LOGGER.warn("Invalid transfer parameters: fromAccountId={}, toAccountId={}, amount={}", fromAccountId, toAccountId, amount);
            return false;
        }

        // Prevent transferring to the same account
        if (fromAccountId.equals(toAccountId))
        {
            LOGGER.warn("Cannot transfer to the same account: {}", fromAccountId);
            return false;
        }

        // Validate transfer amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            LOGGER.warn("Invalid transfer amount: {}", amount);
            return false;
        }

        // Get both accounts
        var fromAccount = getAccountById(fromAccountId);
        var toAccount = getAccountById(toAccountId);

        if (fromAccount == null || toAccount == null)
        {
            LOGGER.warn("Account not found: fromAccountId={}, toAccountId={}", fromAccountId, toAccountId);
            return false;
        }

        var firstWriteLock = (fromAccount.getId() < toAccount.getId() ? fromAccount : toAccount).getReadWriteLock().writeLock();
        var secondWriteLock = (fromAccount.getId() < toAccount.getId() ? toAccount : fromAccount).getReadWriteLock().writeLock();

        // Track lock acquisition state
        boolean firstLockAcquired = false;
        boolean secondLockAcquired = false;

        try
        {
            // Try to acquire first lock with timeout (5 seconds)
            firstLockAcquired = firstWriteLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS);
            if (firstLockAcquired)
            {
                try
                {
                    // Try to acquire second lock with timeout (5 seconds)
                    secondLockAcquired = secondWriteLock.tryLock(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (secondLockAcquired)
                    {
                        // Both locks acquired, perform transfer
                        LOGGER.debug("Performing transfer: {} -> {} : {}", fromAccountId, toAccountId, amount);

                        var success = fromAccount.withdrawAmount(amount);
                        if (success)
                        {
                            toAccount.addAmount(amount);
                            // Update both accounts in the repository
                            updateAccount(fromAccount);
                            updateAccount(toAccount);
                            LOGGER.info("Transfer successful: {} -> {} : {}", fromAccountId, toAccountId, amount);
                        } else
                        {
                            LOGGER.warn("Transfer failed - insufficient funds: {} -> {} : {}", fromAccountId, toAccountId, amount);
                        }
                        return success;
                    } else
                    {
                        LOGGER.warn("Failed to acquire second lock for transfer: {} -> {}", fromAccountId, toAccountId);
                    }
                } finally
                {
                    if (secondLockAcquired)
                    {
                        secondWriteLock.unlock();
                    }
                }
            } else
            {
                LOGGER.warn("Failed to acquire first lock for transfer: {} -> {}", fromAccountId, toAccountId);
            }
            // If we reach here, either first or second lock acquisition failed
            return false;
        } catch (InterruptedException interruptedException)
        {
            LOGGER.error("Transfer interrupted: {} -> {} : {}", fromAccountId, toAccountId, amount, interruptedException);
            Thread.currentThread().interrupt(); // Restore interrupted status
            return false;
        } finally
        {
            // Clean up locks in reverse order of acquisition
            if (firstLockAcquired)
            {
                firstWriteLock.unlock();
            }
        }

    }
}
