package com.bank.server.dto;

import com.bank.business.entities.Account;

import java.math.BigDecimal;

public class CreateAccountRequest {
    private Long userId;
    private String accountNumber;
    private BigDecimal initialBalance;
    private Account.AccountType type;

    // Constructors
    public CreateAccountRequest() {}

    public CreateAccountRequest(Long userId, String accountNumber, BigDecimal initialBalance, Account.AccountType type) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.initialBalance = initialBalance;
        this.type = type;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public Account.AccountType getType() { return type; }
    public void setType(Account.AccountType type) { this.type = type; }
}