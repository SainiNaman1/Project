package com.railway.util;

import com.railway.model.Train;
import com.railway.model.enums.TravelClass;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Encapsulates financial fare calculation using BigDecimal.
 * Avoids floating-point rounding errors inherent to double/float arithmetic.
 */
public final class FareCalculator {

    private static final BigDecimal FIXED_RESERVATION_FEE = new BigDecimal("40.00");
    private static final BigDecimal GST_RATE = new BigDecimal("0.05"); // 5% GST

    private FareCalculator() {
    }

    /**
     * Calculates the journey fare for a single passenger.
     *
     * @param train Train being boarded (applies polymorphic type multiplier)
     * @param travelClass Travel class (applies class multiplier)
     * @param distanceKm Distance between origin and destination in kilometers
     * @return Final rounded fare as BigDecimal with 2 decimal places
     */
    public static BigDecimal calculateSinglePassengerFare(Train train, TravelClass travelClass, int distanceKm) {
        if (train == null || travelClass == null) {
            throw new IllegalArgumentException("Train and TravelClass must not be null");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero");
        }

        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal baseRate = train.getBaseFarePerKm();
        BigDecimal classMultiplier = travelClass.getFareMultiplier();
        BigDecimal trainMultiplier = train.calculateBaseMultiplier(); // Polymorphic call

        // Subtotal = Distance * BaseRate * ClassMultiplier * TrainMultiplier
        BigDecimal baseDistanceFare = distance
                .multiply(baseRate)
                .multiply(classMultiplier)
                .multiply(trainMultiplier);

        // Add fixed reservation charge
        BigDecimal subtotal = baseDistanceFare.add(FIXED_RESERVATION_FEE);

        // Apply GST (5%)
        BigDecimal tax = subtotal.multiply(GST_RATE);
        BigDecimal total = subtotal.add(tax);

        // Round to 2 decimal places (standard banking rounding)
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates total fare for multiple passengers.
     */
    public static BigDecimal calculateTotalFare(Train train, TravelClass travelClass, int distanceKm, int passengerCount) {
        if (passengerCount <= 0) {
            throw new IllegalArgumentException("Passenger count must be at least 1");
        }
        BigDecimal singleFare = calculateSinglePassengerFare(train, travelClass, distanceKm);
        return singleFare.multiply(BigDecimal.valueOf(passengerCount)).setScale(2, RoundingMode.HALF_UP);
    }
}
