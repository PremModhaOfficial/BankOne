package com.prem.clientInterface.util;

import java.io.IOException;
import java.util.Scanner;

public record UserCred(String username, String email, String password) {
    public static UserCred getInformation(Scanner sc)   {
            System.out.println("Enter username : ");
            String username = sc.nextLine();
            System.out.println("Enter email : ");
            String email = sc.nextLine();
            System.out.println("Enter password : ");
            String password = sc.nextLine();

            return new UserCred(username, email, password);
    }

}
