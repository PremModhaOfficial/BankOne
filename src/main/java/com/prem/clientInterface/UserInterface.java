package com.prem.clientInterface;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import com.prem.business.entities.Account;
import com.prem.business.entities.User;
import com.prem.business.entities.Account.AccountType;
import com.prem.business.services.AccountService;
import com.prem.business.services.UserService;
import com.prem.clientInterface.util.InputUtil;
import com.prem.clientInterface.util.Repositories;
import com.prem.clientInterface.util.UserCred;

public class UserInterface extends BankInterface {

    private Scanner sc;

    User user;
    private List<Account> account;

    public UserInterface(UserCred information, Repositories repositories, Scanner sc) {
        this.sc = sc;
        this.userService = new UserService(repositories.userRepository());
        this.accountService = new AccountService(repositories.accountRepository());

        var mayBeUser = userService.getUserByUsername(information.username());

        if (mayBeUser.isPresent()) {
            user = mayBeUser.get();
            if (!authenticate()) {
                return;
            }
        } else {
            System.out.println("User Not Found");
            System.out.println("Creating User...");

            this.user = userService.createUser(information.username(), information.email(),
                    information.password());

            lauchTransactionWindow();
        }
    }

    private boolean authenticate() {
        String resp;
        int attempts = 3;
        try (Scanner scanner = new Scanner(System.in)) {
            resp = scanner.nextLine();
            while (attempts-- >= 0) {
                if (user.getHashedPassword() == resp.hashCode())
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void lauchTransactionWindow() {
        int choice;
        while (true) {
            choice = InputUtil.getIntegerFromUser(sc);
            System.out.println("""
                    1. list and choose the account
                    2. Create new account
                    3. Delete Account
                        """);
            List<Account> accounts = accountService.getAccountsByUserId(user.getId());
            for (Account account : accounts) {
                System.out.println(account);
            }
            this.account = accounts;
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

        accountService.createAccount(user.getId(), "", BigDecimal.valueOf(initialBalance), accountType);
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
