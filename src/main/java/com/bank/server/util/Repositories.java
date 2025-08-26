package com.bank.server.util;

import com.bank.business.repositories.AccountRepository;
import com.bank.business.repositories.UserRepository;

public record Repositories(UserRepository userRepository, AccountRepository accountRepository) {

    @Override
    public UserRepository userRepository() {
        return userRepository;
    }

    @Override
    public AccountRepository accountRepository() {
        return accountRepository;

    }

}
