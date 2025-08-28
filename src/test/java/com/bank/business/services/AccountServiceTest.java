package com.bank.business.services;

import com.bank.business.entities.Account;
import com.bank.business.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest
{

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void testCreateAccount()
    {
        // Arrange
        var userId = 1L;
        var accountNumber = "ACC123456";
        var initialBalance = new BigDecimal("1000.00");
        var type = Account.AccountType.SAVINGS;
        var savedAccount = new Account(userId, accountNumber, initialBalance, type);
        savedAccount.setId(1L); // Simulate ID assignment by repo

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L); // Simulate ID assignment
            return account;
        });

        // Act
        var result = accountService.createAccount(userId, accountNumber, initialBalance, type);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(initialBalance, result.getBalance());
        assertEquals(type, result.getType());
        assertNotNull(result.getId());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testGetAccountById_AccountExists()
    {
        // Arrange
        var accountId = 1L;
        var mockAccount = new Account(1L, "ACC123", BigDecimal.TEN, Account.AccountType.CHECKING);
        mockAccount.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(mockAccount);

        // Act
        var result = accountService.getAccountById(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(mockAccount, result);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testGetAccountById_AccountNotFound()
    {
        // Arrange
        var accountId = 999L;
        when(accountRepository.findById(accountId)).thenReturn(null);

        // Act
        var result = accountService.getAccountById(accountId);

        // Assert
        assertNull(result);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testGetAccountsByUserId()
    {
        // Arrange
        var userId = 1L;
        var acc1 = new Account(userId, "ACC1", BigDecimal.ONE, Account.AccountType.SAVINGS);
        acc1.setId(1L);
        var acc2 = new Account(userId, "ACC2", BigDecimal.TEN, Account.AccountType.CHECKING);
        acc2.setId(2L);
        var mockAccounts = Arrays.asList(acc1, acc2);

        when(accountRepository.findByUserId(userId)).thenReturn(mockAccounts);

        // Act
        var result = accountService.getAccountsByUserId(userId);

        // Assert
        assertEquals(mockAccounts, result);
        verify(accountRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetAccountByAccountNumber_AccountExists()
    {
        // Arrange
        var accountNumber = "ACC999";
        var mockAccount = new Account(2L, accountNumber, BigDecimal.ZERO, Account.AccountType.SAVINGS);
        mockAccount.setId(5L);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(mockAccount);

        // Act
        var result = accountService.getAccountByAccountNumber(accountNumber);

        // Assert
        assertNotNull(result);
        assertEquals(mockAccount, result);
        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void testUpdateAccount()
    {
        // Arrange
        var accountToUpdate = new Account(1L, "OLDACC", BigDecimal.TEN, Account.AccountType.CHECKING);
        accountToUpdate.setId(1L);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var updatedAccount = accountService.updateAccount(accountToUpdate);

        // Assert
        assertSame(accountToUpdate, updatedAccount); // Should return the same object instance
        assertEquals(accountToUpdate.getId(), updatedAccount.getId());
        // Verify updatedAt was updated
        verify(accountRepository, times(1)).save(accountToUpdate);
    }

    @Test
    void testDeleteAccount()
    {
        // Arrange
        var accountId = 1L;

        // Act
        accountService.deleteAccount(accountId);

        // Assert
        verify(accountRepository, times(1)).deleteById(accountId);
    }

    @Test
    void testGetAllAccounts()
    {
        // Arrange
        var acc1 = new Account(1L, "ACC1", BigDecimal.ONE, Account.AccountType.SAVINGS);
        acc1.setId(1L);
        var acc2 = new Account(2L, "ACC2", BigDecimal.TEN, Account.AccountType.CHECKING);
        acc2.setId(2L);
        var mockAccounts = Arrays.asList(acc1, acc2);

        when(accountRepository.getAll()).thenReturn(mockAccounts);

        // Act
        var result = accountService.getAllAccounts();

        // Assert
        assertEquals(mockAccounts, result);
        verify(accountRepository, times(1)).getAll();
    }
}
