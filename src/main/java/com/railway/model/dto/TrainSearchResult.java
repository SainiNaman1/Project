package com.railway.model.dto;

import com.railway.model.RouteStop;
import com.railway.model.Train;
import com.railway.model.enums.TravelClass;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Data Transfer Object (DTO) aggregating train details, route stops,
 * dynamic distance, fare estimation, and seat availability for search results.
 */
public class TrainSearchResult {
    private Train train;
    private RouteStop sourceStop;
    private RouteStop destStop;
    private int distanceKm;
    private Map<TravelClass, Integer> availableSeats = new EnumMap<>(TravelClass.class);
    private Map<TravelClass, BigDecimal> estimatedFares = new EnumMap<>(TravelClass.class);

    public TrainSearchResult() {
    }

    public TrainSearchResult(Train train, RouteStop sourceStop, RouteStop destStop, int distanceKm) {
        this.train = train;
        this.sourceStop = sourceStop;
        this.destStop = destStop;
        this.distanceKm = distanceKm;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public RouteStop getSourceStop() {
        return sourceStop;
    }

    public void setSourceStop(RouteStop sourceStop) {
        this.sourceStop = sourceStop;
    }

    public RouteStop getDestStop() {
        return destStop;
    }

    public void setDestStop(RouteStop destStop) {
        this.destStop = destStop;
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(int distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Map<TravelClass, Integer> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Map<TravelClass, Integer> availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Map<TravelClass, BigDecimal> getEstimatedFares() {
        return estimatedFares;
    }

    public void setEstimatedFares(Map<TravelClass, BigDecimal> estimatedFares) {
        this.estimatedFares = estimatedFares;
    }
}
