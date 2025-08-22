package com.bank.server.dto;

import java.math.BigDecimal;

public class TransactionResponse {
    private boolean success;
    private String message;
    private BigDecimal newBalance;

    // Constructors
    public TransactionResponse() {}

    public TransactionResponse(boolean success, String message, BigDecimal newBalance) {
        this.success = success;
        this.message = message;
        this.newBalance = newBalance;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
}