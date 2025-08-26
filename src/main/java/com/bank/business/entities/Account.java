package com.bank.business.entities;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {
    private Long id;
    private Long userId; // Foreign key to User
    private String accountNumber;
    private AtomicReference<BigDecimal> balance;
    private AccountType type; // e.g., SAVINGS, CHECKING

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public enum AccountType {
        SAVINGS, CHECKING
    }

    // Constructors
    public Account() {
    }

    public Account(Long userId, BigDecimal balance, AccountType type) {
        this.userId = userId;
        this.balance = new AtomicReference<BigDecimal>(balance);
        this.type = type;
        // Account number will be generated when ID is set
    }

    public Account(Long userId, String accountNumber, BigDecimal balance, AccountType type) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = new AtomicReference<BigDecimal>(balance);
        this.type = type;
    }

    // Method to generate account number based on ID
    public void generateAccountNumber() {
        if (this.id != null && this.accountNumber == null) {
            this.accountNumber = "ACC" + String.format("%06d", this.id);
        }
    }

    public void addAmount(BigDecimal amount) {
        while (true) {
            BigDecimal currentBalance = this.balance.get();
            BigDecimal newBalance = currentBalance.add(amount);

            if (this.balance.compareAndSet(currentBalance, newBalance)) {
                return;
            } else {
                LockSupport.parkNanos(1);
            }
        }
    }

    public boolean withdrawAmount(BigDecimal amount) {
        while (true) {
            BigDecimal currentBalance = this.balance.get();
            BigDecimal newBalance = currentBalance.subtract(amount);
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
        // Generate account number when ID is set
        if (this.accountNumber == null) {
            generateAccountNumber();
        }
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

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", type=" + type +
                '}';
    }
}
