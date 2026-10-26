package com.railway.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique 10-digit Passenger Name Record (PNR) numbers.
 * Uses ThreadLocalRandom for thread-safe pseudo-random numeric generation.
 */
public final class PNRGenerator {

    private PNRGenerator() {
    }

    /**
     * Generates a 10-digit numeric PNR string.
     * The first digit is chosen between 2 and 9, followed by 9 random digits.
     */
    public static String generatePNR() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long leadingDigit = random.nextLong(2, 10); // 2 through 9
        long remainingNineDigits = random.nextLong(100_000_000L, 1_000_000_000L);
        return String.valueOf(leadingDigit) + remainingNineDigits;
    }

    /**
     * Validates whether a candidate string conforms to 10-digit PNR format.
     */
    public static boolean isValidPNR(String pnr) {
        return pnr != null && pnr.matches("^[2-9]\\d{9}$");
    }
}
