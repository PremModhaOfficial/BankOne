package com.prem.clientInterface.util;

import com.prem.business.repositories.AccountRepository;
import com.prem.business.repositories.UserRepository;

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
