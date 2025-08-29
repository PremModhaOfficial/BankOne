package com.bank.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bank.business.entities.Account;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.db.inmemory.InMemoryAccountRepository;
import com.bank.db.inmemory.InMemoryUserRepository;

public class SimpleConcurrentTest
{

    private UserService userService;
    private AccountService accountService;

    @BeforeEach
    public void setUp()
    {
        // Set up in-memory repositories
        var userRepository = InMemoryUserRepository.getInstance();
        var accountRepository = InMemoryAccountRepository.getInstance();

        // Set up services
        userService = new UserService(userRepository);
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void testConcurrentAccountDeposits() throws Exception
    {
        // Create a user and account
        var user = userService.createUser("testuser", "test@example.com", "password123");
        var userId = user.getId();

        var account = accountService.createAccount(userId, "ACC123", new BigDecimal("0.0"), Account.AccountType.SAVINGS);
        var accountId = account.getId();

        // Perform concurrent deposits
        var numberOfDeposits = 10;
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
}
