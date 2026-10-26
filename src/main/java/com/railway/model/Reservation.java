package com.railway.model;

import com.railway.model.enums.BookingStatus;
import com.railway.model.enums.TravelClass;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a confirmed or cancelled railway reservation ticket.
 * Demonstrates COMPOSITION (contains a collection of ReservationPassenger instances).
 */
public class Reservation {
    private long id;
    private String pnrNumber;
    private long userId;
    private long trainId;
    private LocalDate journeyDate;
    private Station fromStation;
    private Station toStation;
    private TravelClass travelClass;
    private int totalPassengers;
    private BigDecimal totalFare;
    private BookingStatus status;
    private LocalDateTime bookingTimestamp;
    private List<ReservationPassenger> passengers = new ArrayList<>();

    // Optional navigation reference for joined queries
    private Train train;

    public Reservation() {
    }

    public Reservation(long id, String pnrNumber, long userId, long trainId, LocalDate journeyDate, Station fromStation, Station toStation, TravelClass travelClass, int totalPassengers, BigDecimal totalFare, BookingStatus status, LocalDateTime bookingTimestamp) {
        this.id = id;
        this.pnrNumber = pnrNumber;
        this.userId = userId;
        this.trainId = trainId;
        this.journeyDate = journeyDate;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.travelClass = travelClass;
        this.totalPassengers = totalPassengers;
        this.totalFare = totalFare;
        this.status = status;
        this.bookingTimestamp = bookingTimestamp;
    }

    public void addPassenger(ReservationPassenger passenger) {
        passengers.add(passenger);
    }

    public boolean isCancellable() {
        return status == BookingStatus.CONFIRMED && !journeyDate.isBefore(LocalDate.now());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPnrNumber() {
        return pnrNumber;
    }

    public void setPnrNumber(String pnrNumber) {
        this.pnrNumber = pnrNumber;
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

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public Station getFromStation() {
        return fromStation;
    }

    public void setFromStation(Station fromStation) {
        this.fromStation = fromStation;
    }

    public Station getToStation() {
        return toStation;
    }

    public void setToStation(Station toStation) {
        this.toStation = toStation;
    }

    public TravelClass getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(TravelClass travelClass) {
        this.travelClass = travelClass;
    }

    public int getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(int totalPassengers) {
        this.totalPassengers = totalPassengers;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookingTimestamp() {
        return bookingTimestamp;
    }

    public void setBookingTimestamp(LocalDateTime bookingTimestamp) {
        this.bookingTimestamp = bookingTimestamp;
    }

    public List<ReservationPassenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<ReservationPassenger> passengers) {
        this.passengers = passengers;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    @Override
    public String toString() {
        return "PNR: " + pnrNumber + " (" + status + ") - " + totalPassengers + " Pax";
    }
}
