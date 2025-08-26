package com.bank.server;

import com.bank.business.entities.Account;
import com.bank.business.entities.User;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.db.inmemory.InMemoryAccountRepository;
import com.bank.db.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleConcurrentTest {

    private UserService userService;
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        // Set up in-memory repositories
        InMemoryUserRepository userRepository = InMemoryUserRepository.getInstance();
        InMemoryAccountRepository accountRepository = InMemoryAccountRepository.getInstance();
        
        // Set up services
        userService = new UserService(userRepository);
        accountService = new AccountService(accountRepository);
    }

    @Test
    public void testConcurrentAccountDeposits() throws Exception {
        // Create a user and account
        User user = userService.createUser("testuser", "test@example.com");
        Long userId = user.getId();
        
        Account account = accountService.createAccount(userId, "ACC123", new BigDecimal("0.0"), Account.AccountType.SAVINGS);
        Long accountId = account.getId();
        
        // Perform concurrent deposits
        int numberOfDeposits = 10;
        BigDecimal depositAmount = new BigDecimal("5.0");
        
        CompletableFuture<?>[] depositFutures = IntStream.range(0, numberOfDeposits)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                Account acc = accountService.getAccountById(accountId).orElseThrow();
                acc.addAmount(depositAmount);
                accountService.updateAccount(acc);
            }))
            .toArray(CompletableFuture[]::new);
        
        // Wait for all deposits to complete
        CompletableFuture.allOf(depositFutures).join();
        
        // Verify final balance
        BigDecimal expectedBalance = depositAmount.multiply(new BigDecimal(numberOfDeposits));
        Account updatedAccount = accountService.getAccountById(accountId).orElseThrow();
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()), "Final balance should match expected amount");
    }
}
