package com.bank.infrastructure.persistence.inmemory;

import com.bank.business.entities.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountRepositoryTest {

    private InMemoryAccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = InMemoryAccountRepository.getInstance();
    }

    @Test
    void testSave_NewAccount_AssignsId() {
        // Arrange
        Account account = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.SAVINGS);


        // Act
        Account savedAccount = accountRepository.save(account);

        // Assert
        assertNotNull(savedAccount.getId());
        assertTrue(savedAccount.getId() > 0);
        assertSame(account, savedAccount); // Should return the same instance
    }

    @Test
    void testSave_ExistingAccount_Updates() {
        // Arrange
        var initialBalance = BigDecimal.TEN;
        Account account = new Account(1L, "ACC123", initialBalance, Account.AccountType.SAVINGS);
        Account savedAccount = accountRepository.save(account); // ID assigned here
        Long assignedId = savedAccount.getId();
        BigDecimal additionalBalance = new BigDecimal("50.00");
        savedAccount.addAmount(additionalBalance);

        // Act
        Account updatedAccount = accountRepository.save(savedAccount);

        // Assert
        assertSame(savedAccount, updatedAccount);
        assertEquals(assignedId, updatedAccount.getId());
        assertEquals(additionalBalance.add(initialBalance).compareTo(updatedAccount.getBalance()),0);
    }

    @Test
    void testFindById_AccountExists() {
        // Arrange
        Account account = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.SAVINGS);
        Account savedAccount = accountRepository.save(account);
        Long id = savedAccount.getId();

        // Act
        Optional<Account> foundAccount = accountRepository.findById(id);

        // Assert
        assertTrue(foundAccount.isPresent());
        assertEquals(savedAccount, foundAccount.get());
    }

    @Test
    void testFindById_AccountNotExists() {
        // Arrange
        Long nonExistentId = 999L;

        // Act
        Optional<Account> foundAccount = accountRepository.findById(nonExistentId);

        // Assert
        assertFalse(foundAccount.isPresent());
    }

    @Test
    void testFindByUserId() {
        // Arrange
        Long userId1 = 100000000L;
        Long userId2 = 200000000L;
        Account acc1 = new Account(userId1, "ACC1", BigDecimal.ONE, Account.AccountType.SAVINGS);
        Account acc2 = new Account(userId1, "ACC2", BigDecimal.TEN, Account.AccountType.CHECKING);
        Account acc3 = new Account(userId2, "ACC3", BigDecimal.ZERO, Account.AccountType.SAVINGS);
        accountRepository.save(acc1);
        accountRepository.save(acc2);
        accountRepository.save(acc3);

        // Act
        List<Account> accountsForUser1 = accountRepository.findByUserId(userId1);

        // Assert
        assertEquals(2, accountsForUser1.size());
        assertTrue(accountsForUser1.contains(acc1));
        assertTrue(accountsForUser1.contains(acc2));
        assertFalse(accountsForUser1.contains(acc3));
    }

    @Test
    void testFindByAccountNumber_AccountExists() {
        // Arrange
        String accountNumber = "UNIQUE_ACC_NUM";
        Account account = new Account(1L, accountNumber, BigDecimal.ZERO, Account.AccountType.CHECKING);
        accountRepository.save(account);

        // Act
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(accountNumber);

        // Assert
        assertTrue(foundAccount.isPresent());
        assertEquals(account, foundAccount.get());
    }

    @Test
    void testFindByAccountNumber_AccountNotExists() {
        // Arrange
        String nonExistentAccountNumber = "NON_EXISTENT";

        // Act
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(nonExistentAccountNumber);

        // Assert
        assertFalse(foundAccount.isPresent());
    }

    @Test
    void testDeleteById_AccountExists() {
        // Arrange
        Account account = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.SAVINGS);
        Account savedAccount = accountRepository.save(account);
        Long id = savedAccount.getId();

        // Act
        accountRepository.deleteById(id);

        // Assert
        assertFalse(accountRepository.findById(id).isPresent());
    }

    @Test
    void testDeleteById_AccountNotExists() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> accountRepository.deleteById(nonExistentId));
    }
}
