package com.railway.model;

import com.railway.model.enums.TrainType;
import java.math.BigDecimal;

/**
 * Represents a premier fully air-conditioned superfast express with onboard catering.
 * Demonstrates POLYMORPHISM by overriding fare calculation and speed priority.
 */
public class RajdhaniExpress extends Train {

    public RajdhaniExpress() {
        this.trainType = TrainType.RAJDHANI;
    }

    public RajdhaniExpress(long id, String trainNumber, String trainName, Station sourceStation, Station destStation, BigDecimal baseFarePerKm, boolean active) {
        super(id, trainNumber, trainName, TrainType.RAJDHANI, sourceStation, destStation, baseFarePerKm, active);
    }

    @Override
    public BigDecimal calculateBaseMultiplier() {
        // 50% premium surcharge for priority express and onboard meals
        return new BigDecimal("1.50");
    }

    @Override
    public int getSpeedPriority() {
        return 1; // Highest track priority
    }
}
