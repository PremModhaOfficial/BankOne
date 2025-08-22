package com.bank.business.entities.dto;

import com.bank.business.entities.Account;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a new Account.
 * This DTO excludes server-managed fields like id, createdAt, and updatedAt.
 */
public class AccountCreationRequest {
    private Long userId; // Foreign key to User
    private String accountNumber;
    private BigDecimal initialBalance;
    private Account.AccountType type; // e.g., SAVINGS, CHECKING

    // Constructors
    public AccountCreationRequest() {}

    public AccountCreationRequest(Long userId, String accountNumber, BigDecimal initialBalance, Account.AccountType type) {
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