package com.railway.model;

import com.railway.model.enums.TrainType;
import java.math.BigDecimal;

/**
 * Represents a standard long-distance mail/express service.
 * Baseline fare multiplier is 1.00.
 */
public class MailExpressTrain extends Train {

    public MailExpressTrain() {
        this.trainType = TrainType.MAIL_EXPRESS;
    }

    public MailExpressTrain(long id, String trainNumber, String trainName, Station sourceStation, Station destStation, BigDecimal baseFarePerKm, boolean active) {
        super(id, trainNumber, trainName, TrainType.MAIL_EXPRESS, sourceStation, destStation, baseFarePerKm, active);
    }

    @Override
    public BigDecimal calculateBaseMultiplier() {
        return new BigDecimal("1.00"); // Baseline standard tariff
    }

    @Override
    public int getSpeedPriority() {
        return 3;
    }
}
