package com.bank.db.inmemory;

import com.bank.business.entities.Account;
import com.bank.business.repositories.AccountRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryAccountRepository implements AccountRepository {
    private final Map<Long, Account> accountStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1); // Simple ID generator

    private InMemoryAccountRepository() {
    }

    public static InMemoryAccountRepository getInstance() {
        return InMemoryAccountRepositorySingleton.INSTANCE;
    }

    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            account.setId(idGenerator.getAndIncrement());
        }
        accountStore.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(accountStore.get(id));
    }

    @Override
    public List<Account> findByUserId(Long userId) {
        return accountStore.values().stream()
                .filter(account -> userId.equals(account.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountStore.values().stream()
                .filter(account -> accountNumber.equals(account.getAccountNumber()))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        accountStore.remove(id);
    }

    @Override
    public List<Account> getAll() {
        return accountStore.values().stream().toList();
    }

    private static class InMemoryAccountRepositorySingleton {
        private static final InMemoryAccountRepository INSTANCE = new InMemoryAccountRepository();
    }
}
