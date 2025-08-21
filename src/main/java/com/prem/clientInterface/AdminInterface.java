package com.prem.clientInterface;

import java.util.Scanner;

import com.prem.business.entities.User;
import com.prem.business.services.AccountService;
import com.prem.business.services.UserService;
import com.prem.clientInterface.util.Repositories;
import com.prem.clientInterface.util.UserCred;

public class AdminInterface extends BankInterface {

    User admin;
    private Scanner sc;

    public AdminInterface(UserCred information, Repositories repositoryPair, Scanner sc) {
        this.sc = sc;
        this.userService = new UserService(repositoryPair.userRepository());
        this.accountService = new AccountService(repositoryPair.accountRepository());

        var mayBeUser = userService.getUserByUsername(information.username());

        if (mayBeUser.isPresent()) {
            admin = mayBeUser.get();
            if (!authenticate()) {
                return;
            }
        } else {
            System.out.println("User Not Found");
        }
    }

    private boolean authenticate() {
        String resp;
        int attempts = 3;
        try (Scanner scanner = new Scanner(System.in)) {
            resp = scanner.nextLine();
            while (attempts-- >= 0) {
                if (admin.getHashedPassword() == resp.hashCode())
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void lauchTransactionWindow() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lauchTransactionWindow'");
    }
}
