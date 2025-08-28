package com.bank.server;

import com.bank.business.entities.Account;
import com.bank.business.entities.User;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.db.inmemory.InMemoryAccountRepository;
import com.bank.db.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentAccountOperationsTest
{

    private UserService userService;
    private AccountService accountService;

    @BeforeEach
    public void setUp()
    {
        // Clear any existing data in the singleton instances
        var userRepository = InMemoryUserRepository.getInstance();
        var accountRepository = InMemoryAccountRepository.getInstance();

        // Use reflection to access the internal stores and clear them
        try
        {
            var userStoreField = InMemoryUserRepository.class.getDeclaredField("userStore");
            userStoreField.setAccessible(true);
            ((Map) userStoreField.get(userRepository)).clear();

            var accountStoreField = InMemoryAccountRepository.class.getDeclaredField("accountStore");
            accountStoreField.setAccessible(true);
            ((Map) accountStoreField.get(accountRepository)).clear();

            // Reset the ID generators
            var userIdGeneratorField = InMemoryUserRepository.class.getDeclaredField("idGenerator");
            userIdGeneratorField.setAccessible(true);
            ((AtomicLong) userIdGeneratorField.get(userRepository)).set(1);

            var accountIdGeneratorField = InMemoryAccountRepository.class.getDeclaredField("idGenerator");
            accountIdGeneratorField.setAccessible(true);
            ((AtomicLong) accountIdGeneratorField.get(accountRepository)).set(1);
        } catch (Exception exception)
        {
            throw new RuntimeException("Failed to clear singleton instances", exception);
        }

        // Set up services with the cleared instances
        userService = new UserService(userRepository);
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void testConcurrentAccountDeposits() throws Exception
    {
        // Create a user and account
        var user = userService.createUser("testuser", "test@example.com");
        var userId = user.getId();

        var account = accountService.createAccount(userId, new BigDecimal("0.0"), Account.AccountType.SAVINGS);
        var accountId = account.getId();

        // Perform concurrent deposits
        var numberOfDeposits = 100;
        var depositAmount = new BigDecimal("5.0");

        var depositFutures = IntStream.range(0, numberOfDeposits).mapToObj(i -> CompletableFuture.runAsync(() -> {
            var acc = accountService.getAccountById(accountId);
            if (acc != null)
            {
                acc.addAmount(depositAmount);
                accountService.updateAccount(acc);
            }
        })).toArray(CompletableFuture[]::new);

        // Wait for all deposits to complete
        CompletableFuture.allOf(depositFutures).join();

        // Verify final balance
        var expectedBalance = depositAmount.multiply(new BigDecimal(numberOfDeposits));
        var updatedAccount = accountService.getAccountById(accountId);
        assertNotNull(updatedAccount, "Account should exist");
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()), "Final balance should match expected amount");
    }

    @Test
    public void testConcurrentAccountWithdrawals() throws Exception
    {
        // Create a user and account with initial balance
        var user = userService.createUser("testuser", "test@example.com");
        var userId = user.getId();
        var initialBalance = new BigDecimal("1000.0");

        var account = accountService.createAccount(userId, initialBalance, Account.AccountType.SAVINGS);
        var accountId = account.getId();

        // Perform concurrent withdrawals
        var numberOfWithdrawals = 10;
        var withdrawalAmount = new BigDecimal("5.0");

        var withdrawalFutures = IntStream.range(0, numberOfWithdrawals).mapToObj(i -> CompletableFuture.runAsync(() -> {
            var acc = accountService.getAccountById(accountId);
            if (acc != null)
            {
                acc.withdrawAmount(withdrawalAmount);
                accountService.updateAccount(acc);
            }
        })).toArray(CompletableFuture[]::new);

        // Wait for all withdrawals to complete
        CompletableFuture.allOf(withdrawalFutures).join();

        // Verify final balance
        var expectedBalance = initialBalance.subtract(withdrawalAmount.multiply(new BigDecimal(numberOfWithdrawals)));
        var updatedAccount = accountService.getAccountById(accountId);
        assertNotNull(updatedAccount, "Account should exist");
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()), "Final balance should match expected amount");
    }

    @Test
    public void testConcurrentTransfersBetweenAccounts() throws Exception
    {
        // Create users and accounts
        var user1 = userService.createUser("user1", "user1@example.com");
        var user2 = userService.createUser("user2", "user2@example.com");

        var userId1 = user1.getId();
        var userId2 = user2.getId();

        var initialBalance1 = new BigDecimal("1000.0");
        var initialBalance2 = new BigDecimal("500.0");

        var account1 = accountService.createAccount(userId1, initialBalance1, Account.AccountType.SAVINGS);
        var account2 = accountService.createAccount(userId2, initialBalance2, Account.AccountType.SAVINGS);

        var accountId1 = account1.getId();
        var accountId2 = account2.getId();

        // Perform concurrent transfers from account1 to account2
        var numberOfTransfers = 50;
        var transferAmount = new BigDecimal("10.0");

        var transferFutures = IntStream.range(0, numberOfTransfers).mapToObj(i -> CompletableFuture.runAsync(() -> {
            var fromAccount = accountService.getAccountById(accountId1);
            var toAccount = accountService.getAccountById(accountId2);

            if (fromAccount != null && toAccount != null)
            {
                var success = fromAccount.withdrawAmount(transferAmount);
                if (success)
                {
                    toAccount.addAmount(transferAmount);
                    accountService.updateAccount(fromAccount);
                    accountService.updateAccount(toAccount);
                } else
                {
                    throw new RuntimeException("Insufficient funds for transfer");
                }
            }
        })).toArray(CompletableFuture[]::new);

        // Wait for all transfers to complete
        CompletableFuture.allOf(transferFutures).join();

        // Verify final balances
        var expectedBalance1 = initialBalance1.subtract(transferAmount.multiply(new BigDecimal(numberOfTransfers)));
        var expectedBalance2 = initialBalance2.add(transferAmount.multiply(new BigDecimal(numberOfTransfers)));

        var updatedAccount1 = accountService.getAccountById(accountId1);
        var updatedAccount2 = accountService.getAccountById(accountId2);

        assertNotNull(updatedAccount1, "Account1 should exist");
        assertNotNull(updatedAccount2, "Account2 should exist");

        assertEquals(0, expectedBalance1.compareTo(updatedAccount1.getBalance()), "Final balance of account1 should match expected amount");
        assertEquals(0, expectedBalance2.compareTo(updatedAccount2.getBalance()), "Final balance of account2 should match expected amount");
    }

    @Test
    public void testConcurrentTransfersFromSameAccount() throws Exception
    {
        // Create users and accounts
        var user1 = userService.createUser("user1", "user1@example.com");
        var user2 = userService.createUser("user2", "user2@example.com");
        var user3 = userService.createUser("user3", "user3@example.com");

        var userId1 = user1.getId();
        var userId2 = user2.getId();
        var userId3 = user3.getId();

        var initialBalance1 = new BigDecimal("2000.0"); // Enough for all transfers
        var initialBalance2 = new BigDecimal("500.0");
        var initialBalance3 = new BigDecimal("500.0");

        var account1 = accountService.createAccount(userId1, initialBalance1, Account.AccountType.SAVINGS);
        var account2 = accountService.createAccount(userId2, initialBalance2, Account.AccountType.SAVINGS);
        var account3 = accountService.createAccount(userId3, initialBalance3, Account.AccountType.SAVINGS);

        var accountId1 = account1.getId();
        var accountId2 = account2.getId();
        var accountId3 = account3.getId();

        // Perform concurrent transfers from account1
        var numberOfTransfers = 100;
        var transferAmount = new BigDecimal("10.0");

        var latch = new CountDownLatch(numberOfTransfers);

        // Wait for all transfers to complete
        latch.await();

        // Verify final balances
        var totalTransferred = transferAmount.multiply(new BigDecimal(numberOfTransfers));
        var expectedBalance1 = initialBalance1.subtract(totalTransferred);
        var expectedBalance2 = initialBalance2.add(transferAmount.multiply(new BigDecimal(numberOfTransfers / 2)));
        var expectedBalance3 = initialBalance3.add(transferAmount.multiply(new BigDecimal((numberOfTransfers + 1) / 2)));

        var updatedAccount1 = accountService.getAccountById(accountId1);
        var updatedAccount2 = accountService.getAccountById(accountId2);
        var updatedAccount3 = accountService.getAccountById(accountId3);

        assertEquals(0, expectedBalance1.compareTo(updatedAccount1.getBalance()), "Final balance of account1 should match expected amount");
        assertEquals(0, expectedBalance2.compareTo(updatedAccount2.getBalance()), "Final balance of account2 should match expected amount");
        assertEquals(0, expectedBalance3.compareTo(updatedAccount3.getBalance()), "Final balance of account3 should match expected amount");
    }
}
