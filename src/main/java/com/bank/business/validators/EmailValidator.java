package com.bank.business.validators;

import java.util.regex.Pattern;

public class EmailValidator
{

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email)
    {
        if (email == null || email.isEmpty())
        {
            return false;
        }
        var matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
