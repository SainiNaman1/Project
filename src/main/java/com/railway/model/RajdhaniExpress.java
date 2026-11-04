package com.railway.model;

import java.math.BigDecimal;

import com.railway.model.enums.TrainType;

public class RajdhaniExpress extends Train {

    public RajdhaniExpress() {
        this.trainType = TrainType.RAJDHANI;
    }

    public RajdhaniExpress(long id, String trainNumber, String trainName, Station sourceStation, Station destStation, BigDecimal baseFarePerKm, boolean active) {
        super(id, trainNumber, trainName, TrainType.RAJDHANI, sourceStation, destStation, baseFarePerKm, active);
    }

    @Override
    public BigDecimal calculateBaseMultiplier() {

        return new BigDecimal("1.50");
    }

    @Override
    public int getSpeedPriority() {
        return 1;
    }
}
