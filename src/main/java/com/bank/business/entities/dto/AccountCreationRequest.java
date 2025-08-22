package com.bank.business.entities.dto;

import com.bank.business.entities.Account;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a new Account.
 * This DTO excludes server-managed fields like id, createdAt, and updatedAt.
 */
public class AccountCreationRequest {
    private Long userId; // Foreign key to User
    private String accountNumber; // Optional - if not provided, will be auto-generated
    private BigDecimal initialBalance;
    private Account.AccountType type; // e.g., SAVINGS, CHECKING

    // Constructors
    public AccountCreationRequest() {
    }

    // Constructor without accountNumber for auto-generation
    public AccountCreationRequest(Long userId, BigDecimal initialBalance, Account.AccountType type) {
        this.userId = userId;
        this.initialBalance = initialBalance;
        this.type = type;
    }

    public AccountCreationRequest(Long userId, String accountNumber, BigDecimal initialBalance,
            Account.AccountType type) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.initialBalance = initialBalance;
        this.type = type;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Account.AccountType getType() {
        return type;
    }

    public void setType(Account.AccountType type) {
        this.type = type;
    }
    
    // Helper method to check if accountNumber is provided
    public boolean hasAccountNumber() {
        return this.accountNumber != null && !this.accountNumber.isEmpty();
    }

    @Override
    public String toString() {
        return "AccountCreationRequest{" +
                "userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", initialBalance=" + initialBalance +
                ", type=" + type +
                '}';
    }
}
