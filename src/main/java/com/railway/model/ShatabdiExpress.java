package com.railway.model;

import com.railway.model.enums.TrainType;
import java.math.BigDecimal;

/**
 * Represents a daytime intercity express with chair car configurations.
 * Demonstrates POLYMORPHISM.
 */
public class ShatabdiExpress extends Train {

    public ShatabdiExpress() {
        this.trainType = TrainType.SHATABDI;
    }

    public ShatabdiExpress(long id, String trainNumber, String trainName, Station sourceStation, Station destStation, BigDecimal baseFarePerKm, boolean active) {
        super(id, trainNumber, trainName, TrainType.SHATABDI, sourceStation, destStation, baseFarePerKm, active);
    }

    @Override
    public BigDecimal calculateBaseMultiplier() {
        // 35% premium for intercity high-speed travel
        return new BigDecimal("1.35");
    }

    @Override
    public int getSpeedPriority() {
        return 2;
    }
}
