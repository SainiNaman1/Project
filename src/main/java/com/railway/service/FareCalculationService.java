package com.railway.service;

import com.railway.model.Train;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.TravelClass;
import java.math.BigDecimal;

public interface FareCalculationService {

    FareBreakdownDTO calculateFareBreakdown(Train train, TravelClass travelClass, int distanceKm, int passengerCount);

    BigDecimal calculateSingleFare(Train train, TravelClass travelClass, int distanceKm);

    BigDecimal calculateTotalFare(Train train, TravelClass travelClass, int distanceKm, int passengerCount);
}
