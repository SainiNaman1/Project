package com.railway.service;

import com.railway.model.Train;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.TravelClass;
import java.math.BigDecimal;

/**
 * Authoritative business service for calculating passenger ticket fares,
 * applying polymorphic train multipliers, class multipliers, reservation fees, and taxes.
 */
public interface FareCalculationService {

    /**
     * Calculates an itemized financial fare breakdown for a booking request.
     */
    FareBreakdownDTO calculateFareBreakdown(Train train, TravelClass travelClass, int distanceKm, int passengerCount);

    /**
     * Calculates total fare for a single passenger.
     */
    BigDecimal calculateSingleFare(Train train, TravelClass travelClass, int distanceKm);

    /**
     * Calculates total fare for multiple passengers.
     */
    BigDecimal calculateTotalFare(Train train, TravelClass travelClass, int distanceKm, int passengerCount);
}
