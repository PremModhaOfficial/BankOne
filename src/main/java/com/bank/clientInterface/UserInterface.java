package com.bank.clientInterface;

import java.util.List;
import java.util.Scanner;

import com.bank.business.entities.Account;
import com.bank.business.entities.User;
import com.bank.business.entities.Account.AccountType;
import com.bank.clientInterface.util.InputUtil;
import com.bank.clientInterface.util.UserCred;

public class UserInterface extends BankInterface {

    private Scanner sc;

    User user;
    private List<Account> account;

    public UserInterface(UserCred information, Scanner sc) {
        this.sc = sc;

    }

    @Override
    protected void launchTransactionWindow() {
        int choice;
        while (true) {
            choice = InputUtil.getIntegerFromUser(sc);
            System.out.println("""
                    1. list and choose the account
                    2. Create new account
                    3. Delete Account
                        """);
            // List<Account> accounts = accountService.getAccountsByUserId(user.getId());
            // for (Account account : accounts) {
            // System.out.println(account);
            // }
            // this.account = accounts;
            switch (choice) {
                case 1: // operate Accounts
                    OparateOnAccount();
                    break;

                case 2: // create account
                    createAccount();
                    break;

                case 3: // delete account
                    break;

                default:
                    break;
            }
            continue;
        }
    }

    private void createAccount() {
        System.out.println("Enter account #:");
        account.listIterator().forEachRemaining((acc) -> System.out.println(acc));

        System.out.println("Enter initial balance:");
        double initialBalance = sc.nextDouble();
        sc.nextLine(); // Consume newline left-over

        System.out.println("Select account type:");
        System.out.println("1. CHECKING");
        System.out.println("2. SAVINGS");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline left-over

        AccountType accountType = getAccountFromUser(choice);

        // accountService.createAccount(user.getId(), "",
        // BigDecimal.valueOf(initialBalance), accountType);
    }

    private void OparateOnAccount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'OparateOnAccount'");
    }

    private AccountType getAccountFromUser(int choice) {
        AccountType accountType;
        switch (choice) {
            case 1:
                accountType = AccountType.CHECKING;
                break;
            case 2:
                accountType = AccountType.SAVINGS;
                break;
            default:
                System.out.println("Invalid choice. Defaulting to SAVINGS.");
                accountType = AccountType.SAVINGS;
                break;
        }
        return accountType;
    }
}
