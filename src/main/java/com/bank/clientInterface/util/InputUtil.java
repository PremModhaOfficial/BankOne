package com.bank.clientInterface.util;

import java.util.Scanner;

public class InputUtil {

    public static int getIntegerFromUser(Scanner scanner) {
        int ans;
        while (true) {
            try {
                ans = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return ans;
    }

}
