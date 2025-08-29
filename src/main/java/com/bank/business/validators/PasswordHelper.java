package com.bank.business.validators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHelper
{
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generatePasswordHash(String password)
    {
        try {
            // Generate a random salt
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);

            // Create hash with salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());

            // Combine salt and hash
            byte[] saltAndHash = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
            System.arraycopy(hash, 0, saltAndHash, salt.length, hash.length);

            return Base64.getEncoder().encodeToString(saltAndHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }

    public static boolean matchPassword(String storedHash, String enteredPassword)
    {
        try {
            // Decode the stored hash
            byte[] saltAndHash = Base64.getDecoder().decode(storedHash);
            if (saltAndHash.length != 48) { // 16 bytes salt + 32 bytes SHA-256 hash
                return false; // Invalid hash format
            }

            // Extract salt (first 16 bytes)
            byte[] salt = new byte[16];
            System.arraycopy(saltAndHash, 0, salt, 0, 16);

            // Extract original hash (remaining 32 bytes)
            byte[] originalHash = new byte[32];
            System.arraycopy(saltAndHash, 16, originalHash, 0, 32);

            // Hash the entered password with the same salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] enteredHash = md.digest(enteredPassword.getBytes());

            // Use constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(originalHash, enteredHash);
        } catch (Exception e) {
            return false; // Invalid hash or algorithm error
        }
    }
}
