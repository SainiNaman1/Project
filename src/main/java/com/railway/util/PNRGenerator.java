package com.railway.util;

import java.util.concurrent.ThreadLocalRandom;

public final class PNRGenerator {

    private PNRGenerator() {
    }

    public static String generatePNR() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long leadingDigit = random.nextLong(2, 10);
        long remainingNineDigits = random.nextLong(100_000_000L, 1_000_000_000L);
        return String.valueOf(leadingDigit) + remainingNineDigits;
    }

    public static boolean isValidPNR(String pnr) {
        return pnr != null && pnr.matches("^[2-9]\\d{9}$");
    }
}
