package com.railway.model;

import com.railway.model.enums.TrainType;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Abstract base class for all passenger trains.
 * Demonstrates ABSTRACTION, ENCAPSULATION, and the TEMPLATE/STRATEGY OOP patterns.
 * Specific train types override calculateBaseMultiplier() to implement
 * distinct pricing policies without procedural switch/if-else logic.
 */
public abstract class Train {
    protected long id;
    protected String trainNumber;
    protected String trainName;
    protected TrainType trainType;
    protected Station sourceStation;
    protected Station destStation;
    protected BigDecimal baseFarePerKm;
    protected boolean active;

    public Train() {
    }

    public Train(long id, String trainNumber, String trainName, TrainType trainType, Station sourceStation, Station destStation, BigDecimal baseFarePerKm, boolean active) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.sourceStation = sourceStation;
        this.destStation = destStation;
        this.baseFarePerKm = baseFarePerKm;
        this.active = active;
    }

    /**
     * Polymorphic method: Returns the premium pricing multiplier for this train category.
     * Express trains return 1.00, while Rajdhani/Shatabdi return premium multipliers.
     */
    public abstract BigDecimal calculateBaseMultiplier();

    /**
     * Polymorphic method: Returns the track priority level of this train (1 = highest).
     */
    public abstract int getSpeedPriority();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }

    public Station getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(Station sourceStation) {
        this.sourceStation = sourceStation;
    }

    public Station getDestStation() {
        return destStation;
    }

    public void setDestStation(Station destStation) {
        this.destStation = destStation;
    }

    public BigDecimal getBaseFarePerKm() {
        return baseFarePerKm;
    }

    public void setBaseFarePerKm(BigDecimal baseFarePerKm) {
        this.baseFarePerKm = baseFarePerKm;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Train train)) return false;
        return id == train.id || Objects.equals(trainNumber, train.trainNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trainNumber);
    }

    @Override
    public String toString() {
        return trainNumber + " - " + trainName;
    }
}
