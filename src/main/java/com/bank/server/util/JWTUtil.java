package com.bank.server.util;

import java.util.Base64;

/**
 * A simple JWT utility class for demonstration purposes.
 * In a real application, you should use a proper JWT library like java-jwt or
 * jjwt.
 */
public class JWTUtil {
    private static final String SECRET_KEY = "bankappsecret"; // In a real app, this should be a secure secret
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    /**
     * Generates a simple JWT-like token.
     * 
     * @param userId The user ID to include in the token
     * @return A token string
     */
    public static String generateToken(String userId) {
        // In a real implementation, you would create a proper JWT with header, payload,
        // and signature
        // For this example, we'll just create a simple token with user ID and
        // expiration time
        long now = System.currentTimeMillis();
        long expiry = now + EXPIRATION_TIME;

        // Simple token format: base64(userId) + "." + base64(expiry) + "." +
        // base64(signature)
        String payload = userId + ":" + expiry;
        String signature = createSignature(payload);

        return Base64.getEncoder().encodeToString(userId.getBytes()) + "." +
                Base64.getEncoder().encodeToString(String.valueOf(expiry).getBytes()) + "." +
                Base64.getEncoder().encodeToString(signature.getBytes());
    }

    /**
     * Validates a token.
     * 
     * @param token The token to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\\\.");
            if (parts.length != 3) {
                return false;
            }

            String userId = new String(Base64.getDecoder().decode(parts[0]));
            String expiryStr = new String(Base64.getDecoder().decode(parts[1]));
            String signature = new String(Base64.getDecoder().decode(parts[2]));

            long expiry = Long.parseLong(expiryStr);
            long now = System.currentTimeMillis();

            if (now > expiry) {
                return false; // Token expired
            }

            String payload = userId + ":" + expiry;
            String expectedSignature = createSignature(payload);

            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the user ID from a token.
     * 
     * @param token The token
     * @return The user ID, or null if invalid
     */
    public static String extractUserId(String token) {
        try {
            if (!validateToken(token)) {
                return null;
            }

            String[] parts = token.split("\\\\.");
            return new String(Base64.getDecoder().decode(parts[0]));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a simple signature for the payload.
     * 
     * @param payload The payload to sign
     * @return The signature
     */
    private static String createSignature(String payload) {
        // In a real implementation, you would use HMAC SHA256 or similar
        // For this example, we'll just create a simple hash
        return Integer.toString((payload + SECRET_KEY).hashCode());
    }
}
