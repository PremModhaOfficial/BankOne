package com.bank.clientInterface;

import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;

public abstract class BankInterface {
    protected UserService userService;
    protected AccountService accountService;

    abstract void lauchTransactionWindow();
}
