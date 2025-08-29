package com.bank.db.inmemory;

import com.bank.business.entities.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountRepositoryTest
{

    private InMemoryAccountRepository accountRepository;

    @BeforeEach
    void setUp()
    {
        accountRepository = InMemoryAccountRepository.getInstance();
    }

    @Test
    void testSave_NewAccount_AssignsId()
    {
        // Arrange
        var account = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.SAVINGS);


        // Act
        var savedAccount = accountRepository.save(account);

        // Assert
        assertNotNull(savedAccount.getId());
        assertTrue(savedAccount.getId() > 0);
        assertSame(account, savedAccount); // Should return the same instance
    }

    @Test
    void testSave_ExistingAccount_Updates()
    {
        // Arrange
        var initialBalance = BigDecimal.TEN;
        var account = new Account(1L, "ACC123", initialBalance, Account.AccountType.SAVINGS);
        var savedAccount = accountRepository.save(account); // ID assigned here
        var assignedId = savedAccount.getId();
        var additionalBalance = new BigDecimal("50.00");
        savedAccount.addAmount(additionalBalance);

        // Act
        var updatedAccount = accountRepository.save(savedAccount);

        // Assert
        assertSame(savedAccount, updatedAccount);
        assertEquals(assignedId, updatedAccount.getId());
        assertEquals(additionalBalance.add(initialBalance).compareTo(updatedAccount.getBalance()), 0);
    }

    @Test
    void testFindById_AccountExists()
    {
        // Arrange
        var account = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.SAVINGS);
        var savedAccount = accountRepository.save(account);
        var id = savedAccount.getId();

        // Act
        var foundAccount = accountRepository.findById(id);

        // Assert
        assertNotNull(foundAccount);
        assertEquals(savedAccount, foundAccount);
    }

    @Test
    void testFindById_AccountNotExists()
    {
        // Arrange
        var nonExistentId = 999L;

        // Act
        var foundAccount = accountRepository.findById(nonExistentId);

        // Assert
        assertNull(foundAccount);
    }

    @Test
    void testFindByUserId()
    {
        // Arrange
        var userId1 = 100000000L;
        var userId2 = 200000000L;
        var acc1 = new Account(userId1, "ACC1", BigDecimal.ONE, Account.AccountType.SAVINGS);
        var acc2 = new Account(userId1, "ACC2", BigDecimal.TEN, Account.AccountType.CHECKING);
        var acc3 = new Account(userId2, "ACC3", BigDecimal.ZERO, Account.AccountType.SAVINGS);
        accountRepository.save(acc1);
        accountRepository.save(acc2);
        accountRepository.save(acc3);

        // Act
        var accountsForUser1 = accountRepository.findByUserId(userId1);

        // Assert
        assertEquals(2, accountsForUser1.size());
        assertTrue(accountsForUser1.contains(acc1));
        assertTrue(accountsForUser1.contains(acc2));
        assertFalse(accountsForUser1.contains(acc3));
    }

    @Test
    void testFindByAccountNumber_AccountExists()
    {
        // Arrange
        var accountNumber = "UNIQUE_ACC_NUM";
        var account = new Account(1L, accountNumber, BigDecimal.ZERO, Account.AccountType.CHECKING);
        accountRepository.save(account);

        // Act
        var foundAccount = accountRepository.findByAccountNumber(accountNumber);

        // Assert
        assertNotNull(foundAccount);
        assertEquals(account, foundAccount);
    }

    @Test
    void testFindByAccountNumber_AccountNotExists()
    {
        // Arrange
        var nonExistentAccountNumber = "NON_EXISTENT";

        // Act
        var foundAccount = accountRepository.findByAccountNumber(nonExistentAccountNumber);

        // Assert
        assertNull(foundAccount);
    }

    @Test
    void testDeleteById_AccountExists()
    {
        // Arrange
        var account = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.SAVINGS);
        var savedAccount = accountRepository.save(account);
        var id = savedAccount.getId();

        // Act
        accountRepository.deleteById(id);

        // Assert
        assertNull(accountRepository.findById(id));
    }

    @Test
    void testDeleteById_AccountNotExists()
    {
        // Arrange
        var nonExistentId = 999L;

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> accountRepository.deleteById(nonExistentId));
    }
}
