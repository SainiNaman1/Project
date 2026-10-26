package com.railway.model;

import java.time.LocalTime;

/**
 * Represents an individual scheduled station stop along a train's journey.
 * Demonstrates COMPOSITION (belongs to a train route).
 */
public class RouteStop {
    private long id;
    private long trainId;
    private Station station;
    private int stopSequence;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private int haltMinutes;
    private int distanceFromOriginKm;
    private int scheduledPlatform;

    public RouteStop() {
    }

    public RouteStop(long id, long trainId, Station station, int stopSequence, LocalTime arrivalTime, LocalTime departureTime, int haltMinutes, int distanceFromOriginKm, int scheduledPlatform) {
        this.id = id;
        this.trainId = trainId;
        this.station = station;
        this.stopSequence = stopSequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.haltMinutes = haltMinutes;
        this.distanceFromOriginKm = distanceFromOriginKm;
        this.scheduledPlatform = scheduledPlatform;
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

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getHaltMinutes() {
        return haltMinutes;
    }

    public void setHaltMinutes(int haltMinutes) {
        this.haltMinutes = haltMinutes;
    }

    public int getDistanceFromOriginKm() {
        return distanceFromOriginKm;
    }

    public void setDistanceFromOriginKm(int distanceFromOriginKm) {
        this.distanceFromOriginKm = distanceFromOriginKm;
    }

    public int getScheduledPlatform() {
        return scheduledPlatform;
    }

    public void setScheduledPlatform(int scheduledPlatform) {
        this.scheduledPlatform = scheduledPlatform;
    }
}
