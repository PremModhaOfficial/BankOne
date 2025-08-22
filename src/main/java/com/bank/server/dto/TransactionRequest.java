package com.bank.server.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private Long accountId;
    private BigDecimal amount;

    // Constructors
    public TransactionRequest() {}

    public TransactionRequest(Long accountId, BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}