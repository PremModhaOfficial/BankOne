package com.prem.clientInterface;

import com.prem.business.services.AccountService;
import com.prem.business.services.UserService;

public abstract class BankInterface {
    protected UserService userService;
    protected AccountService accountService;

    abstract protected void lauchTransactionWindow();
}
