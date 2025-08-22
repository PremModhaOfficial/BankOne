package com.bank.business.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Account {
    private Long id;
    private Long userId; // Foreign key to User
    private String accountNumber;
    private AtomicReference<BigDecimal> balance;
    private AccountType type; // e.g., SAVINGS, CHECKING
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum AccountType {
        SAVINGS, CHECKING
    }

    // Constructors
    public Account() {
    }

    public Account(Long userId, String accountNumber, BigDecimal balance, AccountType type) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = new AtomicReference<BigDecimal>(balance);
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addAmmount(BigDecimal ammount) {
        while (true) {
            BigDecimal currentBalance = this.balance.get();
            BigDecimal newBalance = currentBalance.add(ammount);

            if (this.balance.compareAndSet(currentBalance, newBalance)) {
                return;
            } else {
                LockSupport.parkNanos(1);
            }
        }
    }

    public boolean withdrawAmmount(BigDecimal ammount) {
        while (true) {
            BigDecimal currentBalance = this.balance.get();
            BigDecimal newBalance = currentBalance.subtract(ammount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                return false;
            }

            if (this.balance.compareAndSet(currentBalance, newBalance)) {
                return true;
            } else {
                LockSupport.parkNanos(1);
            }
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getBalance() {
        return balance.get();
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
