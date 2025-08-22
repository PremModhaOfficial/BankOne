package com.bank.server.dto;

import com.bank.business.entities.Account;

import java.math.BigDecimal;

public class AccountResponse {
    private Long id;
    private Long userId;
    private String accountNumber;
    private BigDecimal balance;
    private Account.AccountType type;

    // Constructors
    public AccountResponse() {}

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.userId = account.getUserId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
        this.type = account.getType();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Account.AccountType getType() { return type; }
    public void setType(Account.AccountType type) { this.type = type; }
}