package com.railway.model.dto;

import com.railway.model.ReservationPassenger;
import com.railway.model.enums.TravelClass;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) capturing all user inputs required to initiate
 * a transactional ticket booking.
 */
public class BookingRequest {
    private long userId;
    private long trainId;
    private long fromStationId;
    private long toStationId;
    private LocalDate journeyDate;
    private TravelClass travelClass;
    private List<ReservationPassenger> passengers;

    public BookingRequest() {
    }

    public BookingRequest(long userId, long trainId, long fromStationId, long toStationId, LocalDate journeyDate, TravelClass travelClass, List<ReservationPassenger> passengers) {
        this.userId = userId;
        this.trainId = trainId;
        this.fromStationId = fromStationId;
        this.toStationId = toStationId;
        this.journeyDate = journeyDate;
        this.travelClass = travelClass;
        this.passengers = passengers;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTrainId() {
        return trainId;
    }

    public void setTrainId(long trainId) {
        this.trainId = trainId;
    }

    public long getFromStationId() {
        return fromStationId;
    }

    public void setFromStationId(long fromStationId) {
        this.fromStationId = fromStationId;
    }

    public long getToStationId() {
        return toStationId;
    }

    public void setToStationId(long toStationId) {
        this.toStationId = toStationId;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public TravelClass getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(TravelClass travelClass) {
        this.travelClass = travelClass;
    }

    public List<ReservationPassenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<ReservationPassenger> passengers) {
        this.passengers = passengers;
    }
}
