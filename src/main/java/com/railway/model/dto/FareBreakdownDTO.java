package com.railway.model.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareBreakdownDTO {

    private final int distanceKm;
    private final BigDecimal baseFarePerKm;
    private final BigDecimal trainMultiplier;
    private final BigDecimal classMultiplier;
    private final BigDecimal baseDistanceFare;
    private final BigDecimal reservationFee;
    private final BigDecimal gstAmount;
    private final BigDecimal farePerPassenger;
    private final int passengerCount;
    private final BigDecimal totalFare;

    public FareBreakdownDTO(int distanceKm, BigDecimal baseFarePerKm, BigDecimal trainMultiplier,
                            BigDecimal classMultiplier, BigDecimal baseDistanceFare,
                            BigDecimal reservationFee, BigDecimal gstAmount,
                            BigDecimal farePerPassenger, int passengerCount, BigDecimal totalFare) {
        this.distanceKm = distanceKm;
        this.baseFarePerKm = baseFarePerKm.setScale(2, RoundingMode.HALF_UP);
        this.trainMultiplier = trainMultiplier.setScale(2, RoundingMode.HALF_UP);
        this.classMultiplier = classMultiplier.setScale(2, RoundingMode.HALF_UP);
        this.baseDistanceFare = baseDistanceFare.setScale(2, RoundingMode.HALF_UP);
        this.reservationFee = reservationFee.setScale(2, RoundingMode.HALF_UP);
        this.gstAmount = gstAmount.setScale(2, RoundingMode.HALF_UP);
        this.farePerPassenger = farePerPassenger.setScale(2, RoundingMode.HALF_UP);
        this.passengerCount = passengerCount;
        this.totalFare = totalFare.setScale(2, RoundingMode.HALF_UP);
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    public BigDecimal getBaseFarePerKm() {
        return baseFarePerKm;
    }

    public BigDecimal getTrainMultiplier() {
        return trainMultiplier;
    }

    public BigDecimal getClassMultiplier() {
        return classMultiplier;
    }

    public BigDecimal getBaseDistanceFare() {
        return baseDistanceFare;
    }

    public BigDecimal getReservationFee() {
        return reservationFee;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public BigDecimal getFarePerPassenger() {
        return farePerPassenger;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    @Override
    public String toString() {
        return String.format(
                "Fare Breakdown: Distance: %d km | Base: INR %s | Train Mul: %s | Class Mul: %s | Distance Fare: INR %s | Reservation: INR %s | GST: INR %s | Per Passenger: INR %s | Total (%d pax): INR %s",
                distanceKm, baseFarePerKm, trainMultiplier, classMultiplier,
                baseDistanceFare, reservationFee, gstAmount, farePerPassenger,
                passengerCount, totalFare);
    }
}
