package com.railway;

import com.railway.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    @DisplayName("Should successfully hash password and verify valid credentials")
    void testPasswordHashAndVerification() {
        String adminHash = PasswordUtil.hashPassword("Admin@123");
        String rahulHash = PasswordUtil.hashPassword("Rahul@123");
        System.out.println("VALID_ADMIN_HASH=" + adminHash);
        System.out.println("VALID_RAHUL_HASH=" + rahulHash);

        assertTrue(PasswordUtil.checkPassword("Admin@123", adminHash));
        assertTrue(PasswordUtil.checkPassword("Rahul@123", rahulHash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", adminHash));
    }

    @Test
    @DisplayName("Should generate different salt for identical passwords")
    void testSaltUniqueness() {
        String rawPassword = "SamePassword123";
        String hash1 = PasswordUtil.hashPassword(rawPassword);
        String hash2 = PasswordUtil.hashPassword(rawPassword);

        assertNotEquals(hash1, hash2, "BCrypt must generate unique salts for identical passwords");
        assertTrue(PasswordUtil.checkPassword(rawPassword, hash1));
        assertTrue(PasswordUtil.checkPassword(rawPassword, hash2));
    }

    @Test
    @DisplayName("Should throw exception on blank password input")
    void testBlankPasswordValidation() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
    }
}
