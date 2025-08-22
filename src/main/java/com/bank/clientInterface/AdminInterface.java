package com.bank.clientInterface;

import java.util.Scanner;

import com.bank.business.entities.User;
import com.bank.clientInterface.util.UserCred;

public class AdminInterface extends BankInterface {

    User admin;
    private Scanner sc;

    public AdminInterface(UserCred information, Scanner sc) {
        this.sc = sc;

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
    void lauchTransactionWindow() {
        throw new UnsupportedOperationException("Unimplemented method 'lauchTransactionWindow'");
    }
}
