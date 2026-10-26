package com.railway.model;

import com.railway.model.enums.BerthType;
import com.railway.model.enums.TravelClass;

/**
 * Represents a physical seat / berth inside a coach of a train.
 * Demonstrates ENCAPSULATION.
 */
public class Seat {
    private long id;
    private long trainId;
    private TravelClass travelClass;
    private String coachNumber;
    private int seatNumber;
    private BerthType berthType;

    public Seat() {
    }

    public Seat(long id, long trainId, TravelClass travelClass, String coachNumber, int seatNumber, BerthType berthType) {
        this.id = id;
        this.trainId = trainId;
        this.travelClass = travelClass;
        this.coachNumber = coachNumber;
        this.seatNumber = seatNumber;
        this.berthType = berthType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrainId() {
        return trainId;
    }

    public void setTrainId(long trainId) {
        this.trainId = trainId;
    }

    public TravelClass getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(TravelClass travelClass) {
        this.travelClass = travelClass;
    }

    public String getCoachNumber() {
        return coachNumber;
    }

    public void setCoachNumber(String coachNumber) {
        this.coachNumber = coachNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BerthType getBerthType() {
        return berthType;
    }

    public void setBerthType(BerthType berthType) {
        this.berthType = berthType;
    }

    @Override
    public String toString() {
        return coachNumber + "-" + seatNumber + " (" + berthType + ")";
    }
}
