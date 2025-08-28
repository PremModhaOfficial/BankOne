package com.bank.server.config;

import com.bank.business.repositories.AccountRepository;
import com.bank.business.repositories.UserRepository;

public record RepositoryContainer(UserRepository userRepository, AccountRepository accountRepository)
{

    @Override
    public UserRepository userRepository()
    {
        return userRepository;
    }

    @Override
    public AccountRepository accountRepository()
    {
        return accountRepository;

    }

}