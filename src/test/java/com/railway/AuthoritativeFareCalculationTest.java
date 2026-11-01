package com.railway;

import com.railway.model.MailExpressTrain;
import com.railway.model.RajdhaniExpress;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.TravelClass;
import com.railway.service.FareCalculationService;
import com.railway.service.impl.FareCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authoritative Fare Calculation Service Tests")
class AuthoritativeFareCalculationTest {

    private FareCalculationService fareService;
    private Train rajdhaniTrain;
    private Train expressTrain;

    @BeforeEach
    void setUp() {
        fareService = new FareCalculationServiceImpl();
        Station s1 = new Station(1L, "NDLS", "New Delhi", "New Delhi", "Delhi");
        Station s2 = new Station(2L, "BCT", "Mumbai Central", "Mumbai", "Maharashtra");

        rajdhaniTrain = new RajdhaniExpress(101L, "12952", "Mumbai Rajdhani", s1, s2, new BigDecimal("1.40"), true);
        expressTrain = new MailExpressTrain(102L, "12904", "Golden Temple Mail", s1, s2, new BigDecimal("1.00"), true);
    }

    @Test
    @DisplayName("Calculate complete itemized fare breakdown for 1 passenger")
    void testItemizedFareBreakdown() {
        int distanceKm = 1000;
        int paxCount = 1;
        TravelClass travelClass = TravelClass.THIRD_AC; // multiplier 1.60

        // Expected math for Rajdhani:
        // train multiplier = 1.50, baseRate = 1.40, classMultiplier = 1.60
        // distanceFare = 1000 * 1.40 * 1.60 * 1.50 = 3360.00
        // subtotal = 3360.00 + 40.00 (fee) = 3400.00
        // gst (5%) = 3400.00 * 0.05 = 170.00
        // total per pax = 3570.00
        FareBreakdownDTO breakdown = fareService.calculateFareBreakdown(rajdhaniTrain, travelClass, distanceKm, paxCount);

        assertNotNull(breakdown);
        assertEquals(distanceKm, breakdown.getDistanceKm());
        assertEquals(new BigDecimal("1.40"), breakdown.getBaseFarePerKm());
        assertEquals(new BigDecimal("1.50"), breakdown.getTrainMultiplier());
        assertEquals(new BigDecimal("1.60"), breakdown.getClassMultiplier());
        assertEquals(new BigDecimal("3360.00"), breakdown.getBaseDistanceFare());
        assertEquals(new BigDecimal("40.00"), breakdown.getReservationFee());
        assertEquals(new BigDecimal("170.00"), breakdown.getGstAmount());
        assertEquals(new BigDecimal("3570.00"), breakdown.getFarePerPassenger());
        assertEquals(new BigDecimal("3570.00"), breakdown.getTotalFare());
        assertEquals(1, breakdown.getPassengerCount());
    }

    @Test
    @DisplayName("Total fare for multiple passengers must strictly scale with single passenger fare")
    void testMultiplePassengersScaling() {
        int distanceKm = 800;
        TravelClass travelClass = TravelClass.SLEEPER; // multiplier 1.00

        BigDecimal singleFare = fareService.calculateSingleFare(expressTrain, travelClass, distanceKm);
        for (int pax = 2; pax <= 6; pax++) {
            BigDecimal totalFare = fareService.calculateTotalFare(expressTrain, travelClass, distanceKm, pax);
            assertEquals(singleFare.multiply(BigDecimal.valueOf(pax)).setScale(2, RoundingMode.HALF_UP), totalFare);
        }
    }

    @Test
    @DisplayName("Rejects invalid distance or passenger constraints")
    void testInvalidConstraints() {
        assertThrows(IllegalArgumentException.class, () ->
                fareService.calculateFareBreakdown(rajdhaniTrain, TravelClass.FIRST_AC, 0, 1));
        assertThrows(IllegalArgumentException.class, () ->
                fareService.calculateFareBreakdown(rajdhaniTrain, TravelClass.FIRST_AC, -100, 1));
        assertThrows(IllegalArgumentException.class, () ->
                fareService.calculateFareBreakdown(rajdhaniTrain, TravelClass.FIRST_AC, 500, 0));
        assertThrows(IllegalArgumentException.class, () ->
                fareService.calculateFareBreakdown(rajdhaniTrain, TravelClass.FIRST_AC, 500, 7));
        assertThrows(IllegalArgumentException.class, () ->
                fareService.calculateFareBreakdown(null, TravelClass.FIRST_AC, 500, 1));
        assertThrows(IllegalArgumentException.class, () ->
                fareService.calculateFareBreakdown(rajdhaniTrain, null, 500, 1));
    }
}
