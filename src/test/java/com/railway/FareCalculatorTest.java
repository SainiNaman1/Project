package com.railway;

import com.railway.model.MailExpressTrain;
import com.railway.model.RajdhaniExpress;
import com.railway.model.ShatabdiExpress;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.enums.TravelClass;
import com.railway.util.FareCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FareCalculatorTest {

    private Station ndls;
    private Station mmct;

    @BeforeEach
    void setUp() {
        ndls = new Station(1L, "NDLS", "New Delhi", "New Delhi", "Delhi");
        mmct = new Station(2L, "MMCT", "Mumbai Central", "Mumbai", "Maharashtra");
    }

    @Test
    @DisplayName("Polymorphic fare: Rajdhani fare must exceed Standard Mail/Express fare for same distance")
    void testPolymorphicFareCalculation() {
        BigDecimal baseRate = new BigDecimal("1.50");
        int distance = 500;

        Train rajdhani = new RajdhaniExpress(1L, "12952", "Mumbai Rajdhani", ndls, mmct, baseRate, true);
        Train express = new MailExpressTrain(2L, "12902", "Gujarat Mail", ndls, mmct, baseRate, true);

        BigDecimal rajdhaniFare = FareCalculator.calculateSinglePassengerFare(rajdhani, TravelClass.THIRD_AC, distance);
        BigDecimal expressFare = FareCalculator.calculateSinglePassengerFare(express, TravelClass.THIRD_AC, distance);

        assertTrue(rajdhaniFare.compareTo(expressFare) > 0, 
                "Rajdhani fare (" + rajdhaniFare + ") must be higher than express fare (" + expressFare + ") due to premium multiplier");
    }

    @Test
    @DisplayName("Class fare progression: 1A > 2A > 3A > SL")
    void testTravelClassFareHierarchy() {
        BigDecimal baseRate = new BigDecimal("1.20");
        int distance = 400;
        Train express = new MailExpressTrain(1L, "12418", "Prayagraj Express", ndls, mmct, baseRate, true);

        BigDecimal fare1A = FareCalculator.calculateSinglePassengerFare(express, TravelClass.FIRST_AC, distance);
        BigDecimal fare2A = FareCalculator.calculateSinglePassengerFare(express, TravelClass.SECOND_AC, distance);
        BigDecimal fare3A = FareCalculator.calculateSinglePassengerFare(express, TravelClass.THIRD_AC, distance);
        BigDecimal fareSL = FareCalculator.calculateSinglePassengerFare(express, TravelClass.SLEEPER, distance);

        assertTrue(fare1A.compareTo(fare2A) > 0);
        assertTrue(fare2A.compareTo(fare3A) > 0);
        assertTrue(fare3A.compareTo(fareSL) > 0);
    }

    @Test
    @DisplayName("Total fare for N passengers must equal N * single fare")
    void testMultiplePassengerFare() {
        BigDecimal baseRate = new BigDecimal("1.50");
        Train shatabdi = new ShatabdiExpress(1L, "12002", "Bhopal Shatabdi", ndls, mmct, baseRate, true);

        BigDecimal singleFare = FareCalculator.calculateSinglePassengerFare(shatabdi, TravelClass.CHAIR_CAR, 300);
        BigDecimal totalFare4 = FareCalculator.calculateTotalFare(shatabdi, TravelClass.CHAIR_CAR, 300, 4);

        assertEquals(singleFare.multiply(BigDecimal.valueOf(4)), totalFare4);
    }

    @Test
    @DisplayName("Should reject invalid distances or passenger counts")
    void testInputValidations() {
        Train express = new MailExpressTrain(1L, "12418", "Express", ndls, mmct, new BigDecimal("1.00"), true);
        assertThrows(IllegalArgumentException.class, () -> FareCalculator.calculateSinglePassengerFare(express, TravelClass.SLEEPER, 0));
        assertThrows(IllegalArgumentException.class, () -> FareCalculator.calculateSinglePassengerFare(express, TravelClass.SLEEPER, -50));
        assertThrows(IllegalArgumentException.class, () -> FareCalculator.calculateTotalFare(express, TravelClass.SLEEPER, 100, 0));
    }
}
