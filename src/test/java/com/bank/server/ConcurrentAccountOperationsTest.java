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
        InMemoryUserRepository userRepository = InMemoryUserRepository.getInstance();
        InMemoryAccountRepository accountRepository = InMemoryAccountRepository.getInstance();

        // Use reflection to access the internal stores and clear them
        try
        {
            Field userStoreField = InMemoryUserRepository.class.getDeclaredField("userStore");
            userStoreField.setAccessible(true);
            ((Map) userStoreField.get(userRepository)).clear();

            Field accountStoreField = InMemoryAccountRepository.class.getDeclaredField("accountStore");
            accountStoreField.setAccessible(true);
            ((Map) accountStoreField.get(accountRepository)).clear();

            // Reset the ID generators
            Field userIdGeneratorField = InMemoryUserRepository.class.getDeclaredField("idGenerator");
            userIdGeneratorField.setAccessible(true);
            ((AtomicLong) userIdGeneratorField.get(userRepository)).set(1);

            Field accountIdGeneratorField = InMemoryAccountRepository.class.getDeclaredField("idGenerator");
            accountIdGeneratorField.setAccessible(true);
            ((AtomicLong) accountIdGeneratorField.get(accountRepository)).set(1);
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to clear singleton instances", e);
        }

        // Set up services with the cleared instances
        userService = new UserService(userRepository);
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void testConcurrentAccountDeposits() throws Exception
    {
        // Create a user and account
        User user = userService.createUser("testuser", "test@example.com");
        Long userId = user.getId();

        Account account = accountService.createAccount(userId, new BigDecimal("0.0"), Account.AccountType.SAVINGS);
        Long accountId = account.getId();

        // Perform concurrent deposits
        int numberOfDeposits = 100;
        BigDecimal depositAmount = new BigDecimal("5.0");

        CompletableFuture<?>[] depositFutures = IntStream.range(0, numberOfDeposits).mapToObj(i -> CompletableFuture.runAsync(() -> {
            Account acc = accountService.getAccountById(accountId).orElseThrow();
            acc.addAmount(depositAmount);
            accountService.updateAccount(acc);
        })).toArray(CompletableFuture[]::new);

        // Wait for all deposits to complete
        CompletableFuture.allOf(depositFutures).join();

        // Verify final balance
        BigDecimal expectedBalance = depositAmount.multiply(new BigDecimal(numberOfDeposits));
        Account updatedAccount = accountService.getAccountById(accountId).orElseThrow();
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()), "Final balance should match expected amount");
    }

    @Test
    public void testConcurrentAccountWithdrawals() throws Exception
    {
        // Create a user and account with initial balance
        User user = userService.createUser("testuser", "test@example.com");
        Long userId = user.getId();
        BigDecimal initialBalance = new BigDecimal("1000.0");

        Account account = accountService.createAccount(userId, initialBalance, Account.AccountType.SAVINGS);
        Long accountId = account.getId();

        // Perform concurrent withdrawals
        int numberOfWithdrawals = 10;
        BigDecimal withdrawalAmount = new BigDecimal("5.0");

        CompletableFuture<?>[] withdrawalFutures = IntStream.range(0, numberOfWithdrawals).mapToObj(i -> CompletableFuture.runAsync(() -> {
            Account acc = accountService.getAccountById(accountId).orElseThrow();
            acc.withdrawAmount(withdrawalAmount);
            accountService.updateAccount(acc);
        })).toArray(CompletableFuture[]::new);

        // Wait for all withdrawals to complete
        CompletableFuture.allOf(withdrawalFutures).join();

        // Verify final balance
        BigDecimal expectedBalance = initialBalance.subtract(withdrawalAmount.multiply(new BigDecimal(numberOfWithdrawals)));
        Account updatedAccount = accountService.getAccountById(accountId).orElseThrow();
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()), "Final balance should match expected amount");
    }

    @Test
    public void testConcurrentTransfersBetweenAccounts() throws Exception
    {
        // Create users and accounts
        User user1 = userService.createUser("user1", "user1@example.com");
        User user2 = userService.createUser("user2", "user2@example.com");

        Long userId1 = user1.getId();
        Long userId2 = user2.getId();

        BigDecimal initialBalance1 = new BigDecimal("1000.0");
        BigDecimal initialBalance2 = new BigDecimal("500.0");

        Account account1 = accountService.createAccount(userId1, initialBalance1, Account.AccountType.SAVINGS);
        Account account2 = accountService.createAccount(userId2, initialBalance2, Account.AccountType.SAVINGS);

        Long accountId1 = account1.getId();
        Long accountId2 = account2.getId();

        // Perform concurrent transfers from account1 to account2
        int numberOfTransfers = 50;
        BigDecimal transferAmount = new BigDecimal("10.0");

        CompletableFuture<?>[] transferFutures = IntStream.range(0, numberOfTransfers).mapToObj(i -> CompletableFuture.runAsync(() -> {
            Account fromAccount = accountService.getAccountById(accountId1).orElseThrow();
            Account toAccount = accountService.getAccountById(accountId2).orElseThrow();

            boolean success = fromAccount.withdrawAmount(transferAmount);
            if (success)
            {
                toAccount.addAmount(transferAmount);
                accountService.updateAccount(fromAccount);
                accountService.updateAccount(toAccount);
            } else
            {
                throw new RuntimeException("Insufficient funds for transfer");
            }
        })).toArray(CompletableFuture[]::new);

        // Wait for all transfers to complete
        CompletableFuture.allOf(transferFutures).join();

        // Verify final balances
        BigDecimal expectedBalance1 = initialBalance1.subtract(transferAmount.multiply(new BigDecimal(numberOfTransfers)));
        BigDecimal expectedBalance2 = initialBalance2.add(transferAmount.multiply(new BigDecimal(numberOfTransfers)));

        Account updatedAccount1 = accountService.getAccountById(accountId1).orElseThrow();
        Account updatedAccount2 = accountService.getAccountById(accountId2).orElseThrow();

        assertEquals(0, expectedBalance1.compareTo(updatedAccount1.getBalance()), "Final balance of account1 should match expected amount");
        assertEquals(0, expectedBalance2.compareTo(updatedAccount2.getBalance()), "Final balance of account2 should match expected amount");
    }

    @Test
    public void testConcurrentTransfersToSameAccount() throws Exception
    {
        // Create users and accounts
        User user1 = userService.createUser("user1", "user1@example.com");
        User user2 = userService.createUser("user2", "user2@example.com");
        User user3 = userService.createUser("user3", "user3@example.com");

        Long userId1 = user1.getId();
        Long userId2 = user2.getId();
        Long userId3 = user3.getId();

        BigDecimal initialBalance1 = new BigDecimal("1000.0");
        BigDecimal initialBalance2 = new BigDecimal("1000.0");
        BigDecimal initialBalance3 = new BigDecimal("500.0");

        Account account1 = accountService.createAccount(userId1, initialBalance1, Account.AccountType.SAVINGS);
        Account account2 = accountService.createAccount(userId2, initialBalance2, Account.AccountType.SAVINGS);
        Account account3 = accountService.createAccount(userId3, initialBalance3, Account.AccountType.SAVINGS);

        Long accountId1 = account1.getId();
        Long accountId2 = account2.getId();
        Long accountId3 = account3.getId();

        // Perform concurrent transfers to account3
        int numberOfTransfers = 50;
        BigDecimal transferAmount = new BigDecimal("10.0");

        CompletableFuture<?>[] transferFutures = IntStream.range(0, numberOfTransfers).mapToObj(i -> CompletableFuture.runAsync(() -> {
            // Alternate between account1 and account2 as source
            Account fromAccount = (i % 2 == 0) ? accountService.getAccountById(accountId1).orElseThrow() : accountService.getAccountById(accountId2).orElseThrow();
            Account toAccount = accountService.getAccountById(accountId3).orElseThrow();

            boolean success = fromAccount.withdrawAmount(transferAmount);
            if (success)
            {
                toAccount.addAmount(transferAmount);
                accountService.updateAccount(fromAccount);
                accountService.updateAccount(toAccount);
            } else
            {
                throw new RuntimeException("Insufficient funds for transfer");
            }
        })).toArray(CompletableFuture[]::new);

        // Wait for all transfers to complete
        CompletableFuture.allOf(transferFutures).join();

        // Verify final balances
        BigDecimal totalTransferred = transferAmount.multiply(new BigDecimal(numberOfTransfers));
        BigDecimal expectedBalance1 = initialBalance1.subtract(transferAmount.multiply(new BigDecimal(numberOfTransfers / 2)));
        BigDecimal expectedBalance2 = initialBalance2.subtract(transferAmount.multiply(new BigDecimal(numberOfTransfers / 2)));
        BigDecimal expectedBalance3 = initialBalance3.add(totalTransferred);

        Account updatedAccount1 = accountService.getAccountById(accountId1).orElseThrow();
        Account updatedAccount2 = accountService.getAccountById(accountId2).orElseThrow();
        Account updatedAccount3 = accountService.getAccountById(accountId3).orElseThrow();

        assertEquals(0, expectedBalance1.compareTo(updatedAccount1.getBalance()), "Final balance of account1 should match expected amount");
        assertEquals(0, expectedBalance2.compareTo(updatedAccount2.getBalance()), "Final balance of account2 should match expected amount");
        assertEquals(0, expectedBalance3.compareTo(updatedAccount3.getBalance()), "Final balance of account3 should match expected amount");
    }

    @Test
    public void testConcurrentTransfersFromSameAccount() throws Exception
    {
        // Create users and accounts
        User user1 = userService.createUser("user1", "user1@example.com");
        User user2 = userService.createUser("user2", "user2@example.com");
        User user3 = userService.createUser("user3", "user3@example.com");

        Long userId1 = user1.getId();
        Long userId2 = user2.getId();
        Long userId3 = user3.getId();

        BigDecimal initialBalance1 = new BigDecimal("2000.0"); // Enough for all transfers
        BigDecimal initialBalance2 = new BigDecimal("500.0");
        BigDecimal initialBalance3 = new BigDecimal("500.0");

        Account account1 = accountService.createAccount(userId1, initialBalance1, Account.AccountType.SAVINGS);
        Account account2 = accountService.createAccount(userId2, initialBalance2, Account.AccountType.SAVINGS);
        Account account3 = accountService.createAccount(userId3, initialBalance3, Account.AccountType.SAVINGS);

        Long accountId1 = account1.getId();
        Long accountId2 = account2.getId();
        Long accountId3 = account3.getId();

        // Perform concurrent transfers from account1
        int numberOfTransfers = 100;
        BigDecimal transferAmount = new BigDecimal("10.0");

        CountDownLatch latch = new CountDownLatch(numberOfTransfers);

        // Wait for all transfers to complete
        latch.await();

        // Verify final balances
        BigDecimal totalTransferred = transferAmount.multiply(new BigDecimal(numberOfTransfers));
        BigDecimal expectedBalance1 = initialBalance1.subtract(totalTransferred);
        BigDecimal expectedBalance2 = initialBalance2.add(transferAmount.multiply(new BigDecimal(numberOfTransfers / 2)));
        BigDecimal expectedBalance3 = initialBalance3.add(transferAmount.multiply(new BigDecimal((numberOfTransfers + 1) / 2)));

        Account updatedAccount1 = accountService.getAccountById(accountId1).orElseThrow();
        Account updatedAccount2 = accountService.getAccountById(accountId2).orElseThrow();
        Account updatedAccount3 = accountService.getAccountById(accountId3).orElseThrow();

        assertEquals(0, expectedBalance1.compareTo(updatedAccount1.getBalance()), "Final balance of account1 should match expected amount");
        assertEquals(0, expectedBalance2.compareTo(updatedAccount2.getBalance()), "Final balance of account2 should match expected amount");
        assertEquals(0, expectedBalance3.compareTo(updatedAccount3.getBalance()), "Final balance of account3 should match expected amount");
    }
}
