package com.railway.model;

import com.railway.model.enums.BerthType;
import com.railway.model.enums.Gender;

/**
 * Details of an individual passenger traveling under a master Reservation.
 * Demonstrates ENCAPSULATION.
 */
public class ReservationPassenger {
    private long id;
    private long reservationId;
    private String passengerName;
    private int age;
    private Gender gender;
    private BerthType berthPreference;
    private String assignedCoach;
    private int assignedSeatNumber;
    private BerthType assignedBerth;

    public ReservationPassenger() {
    }

    public ReservationPassenger(long id, long reservationId, String passengerName, int age, Gender gender, BerthType berthPreference, String assignedCoach, int assignedSeatNumber, BerthType assignedBerth) {
        this.id = id;
        this.reservationId = reservationId;
        this.passengerName = passengerName;
        this.age = age;
        this.gender = gender;
        this.berthPreference = berthPreference;
        this.assignedCoach = assignedCoach;
        this.assignedSeatNumber = assignedSeatNumber;
        this.assignedBerth = assignedBerth;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BerthType getBerthPreference() {
        return berthPreference;
    }

    public void setBerthPreference(BerthType berthPreference) {
        this.berthPreference = berthPreference;
    }

    public String getAssignedCoach() {
        return assignedCoach;
    }

    public void setAssignedCoach(String assignedCoach) {
        this.assignedCoach = assignedCoach;
    }

    public int getAssignedSeatNumber() {
        return assignedSeatNumber;
    }

    public void setAssignedSeatNumber(int assignedSeatNumber) {
        this.assignedSeatNumber = assignedSeatNumber;
    }

    public BerthType getAssignedBerth() {
        return assignedBerth;
    }

    public void setAssignedBerth(BerthType assignedBerth) {
        this.assignedBerth = assignedBerth;
    }
}
