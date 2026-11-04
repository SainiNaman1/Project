package com.railway.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {

    }

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean checkPassword(String candidatePassword, String storedHash) {
        if (candidatePassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidatePassword, storedHash);
        } catch (IllegalArgumentException e) {

            return false;
        }
    }
}
