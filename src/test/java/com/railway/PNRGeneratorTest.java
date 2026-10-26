package com.railway;

import com.railway.util.PNRGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PNRGeneratorTest {

    @Test
    @DisplayName("Should generate valid 10-digit numeric PNR with leading digit between 2 and 9")
    void testPNRFormat() {
        for (int i = 0; i < 100; i++) {
            String pnr = PNRGenerator.generatePNR();
            assertNotNull(pnr);
            assertEquals(10, pnr.length(), "PNR must be exactly 10 digits");
            assertTrue(PNRGenerator.isValidPNR(pnr), "PNR must conform to valid pattern: " + pnr);
            char firstChar = pnr.charAt(0);
            assertTrue(firstChar >= '2' && firstChar <= '9', "First digit must be between 2 and 9");
        }
    }

    @Test
    @DisplayName("Should reject malformed PNR strings")
    void testInvalidPNRs() {
        assertFalse(PNRGenerator.isValidPNR(null));
        assertFalse(PNRGenerator.isValidPNR(""));
        assertFalse(PNRGenerator.isValidPNR("123456789")); // 9 digits
        assertFalse(PNRGenerator.isValidPNR("12345678901")); // 11 digits
        assertFalse(PNRGenerator.isValidPNR("0123456789")); // starts with 0
        assertFalse(PNRGenerator.isValidPNR("1123456789")); // starts with 1
        assertFalse(PNRGenerator.isValidPNR("ABCDEFGHIJ")); // letters
    }

    @Test
    @DisplayName("Should generate unique PNRs under sequential loops")
    void testUniqueness() {
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            String pnr = PNRGenerator.generatePNR();
            assertTrue(generated.add(pnr), "Collision detected for PNR: " + pnr);
        }
    }
}
