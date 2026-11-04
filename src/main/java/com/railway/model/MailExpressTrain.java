package com.railway.model;

import java.math.BigDecimal;

import com.railway.model.enums.TrainType;

public class MailExpressTrain extends Train {

    public MailExpressTrain() {
        this.trainType = TrainType.MAIL_EXPRESS;
    }

    public MailExpressTrain(long id, String trainNumber, String trainName, Station sourceStation, Station destStation, BigDecimal baseFarePerKm, boolean active) {
        super(id, trainNumber, trainName, TrainType.MAIL_EXPRESS, sourceStation, destStation, baseFarePerKm, active);
    }

    @Override
    public BigDecimal calculateBaseMultiplier() {
        return new BigDecimal("1.00");
    }

    @Override
    public int getSpeedPriority() {
        return 3;
    }
}
