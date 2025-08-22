package com.bank.clientInterface;

import java.util.Scanner;

import com.bank.business.entities.User;
import com.bank.clientInterface.util.UserCred;

public class AdminInterface extends BankInterface {

    User admin;

    public AdminInterface(UserCred information, Scanner sc) {

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
    void launchTransactionWindow() {
        throw new UnsupportedOperationException("Unimplemented method 'lauchTransactionWindow'");
    }
}
