package com.railway.service.impl;

import com.railway.exception.ValidationException;
import com.railway.model.Train;
import com.railway.model.dto.FareBreakdownDTO;
import com.railway.model.enums.TravelClass;
import com.railway.service.FareCalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculationServiceImpl implements FareCalculationService {

    public static final BigDecimal FIXED_RESERVATION_FEE = new BigDecimal("40.00");
    public static final BigDecimal GST_RATE = new BigDecimal("0.05");

    @Override
    public FareBreakdownDTO calculateFareBreakdown(Train train, TravelClass travelClass, int distanceKm, int passengerCount) {
        if (train == null) {
            throw new IllegalArgumentException("Train cannot be null for fare calculation.");
        }
        if (travelClass == null) {
            throw new IllegalArgumentException("Travel class cannot be null for fare calculation.");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero kilometers. Actual: " + distanceKm);
        }
        if (passengerCount <= 0 || passengerCount > 6) {
            throw new IllegalArgumentException("Passenger count must be between 1 and 6. Actual: " + passengerCount);
        }

        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal baseRate = train.getBaseFarePerKm();
        BigDecimal classMultiplier = travelClass.getFareMultiplier();
        BigDecimal trainMultiplier = train.calculateBaseMultiplier();

        BigDecimal baseDistanceFare = distance
                .multiply(baseRate)
                .multiply(classMultiplier)
                .multiply(trainMultiplier);

        BigDecimal subtotal = baseDistanceFare.add(FIXED_RESERVATION_FEE);

        BigDecimal gstAmount = subtotal.multiply(GST_RATE);
        BigDecimal singlePassengerTotal = subtotal.add(gstAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalFare = singlePassengerTotal.multiply(BigDecimal.valueOf(passengerCount)).setScale(2, RoundingMode.HALF_UP);

        return new FareBreakdownDTO(
                distanceKm,
                baseRate,
                trainMultiplier,
                classMultiplier,
                baseDistanceFare,
                FIXED_RESERVATION_FEE,
                gstAmount,
                singlePassengerTotal,
                passengerCount,
                totalFare
        );
    }

    @Override
    public BigDecimal calculateSingleFare(Train train, TravelClass travelClass, int distanceKm) {
        return calculateFareBreakdown(train, travelClass, distanceKm, 1).getFarePerPassenger();
    }

    @Override
    public BigDecimal calculateTotalFare(Train train, TravelClass travelClass, int distanceKm, int passengerCount) {
        return calculateFareBreakdown(train, travelClass, distanceKm, passengerCount).getTotalFare();
    }
}
