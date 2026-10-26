package com.railway.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides cryptographically secure password hashing and verification using BCrypt.
 * Uses a work factor of 12, generating an automatic salt for every hash.
 * This guarantees resistance to precomputed rainbow table and dictionary attacks.
 */
public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Hashes a raw plaintext password using BCrypt with automatic salt generation.
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a candidate raw password against an existing BCrypt hash.
     */
    public static boolean checkPassword(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidatePassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Malformed hash format
            return false;
        }
    }
}
