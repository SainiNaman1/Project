package com.railway.util;

import com.railway.model.Train;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.TravelClass;
import com.railway.service.FareCalculationService;
import com.railway.service.impl.FareCalculationServiceImpl;
import java.math.BigDecimal;

/**
 * Legacy utility wrapper delegating to the authoritative FareCalculationService.
 * Avoids floating-point rounding errors and preserves backward compatibility.
 */
public final class FareCalculator {

    private static final FareCalculationService fareService = new FareCalculationServiceImpl();

    private FareCalculator() {
    }

    public static BigDecimal calculateSinglePassengerFare(Train train, TravelClass travelClass, int distanceKm) {
        return fareService.calculateSingleFare(train, travelClass, distanceKm);
    }

    public static BigDecimal calculateTotalFare(Train train, TravelClass travelClass, int distanceKm, int passengerCount) {
        return fareService.calculateTotalFare(train, travelClass, distanceKm, passengerCount);
    }

    public static FareBreakdownDTO calculateFareBreakdown(Train train, TravelClass travelClass, int distanceKm, int passengerCount) {
        return fareService.calculateFareBreakdown(train, travelClass, distanceKm, passengerCount);
    }
}
