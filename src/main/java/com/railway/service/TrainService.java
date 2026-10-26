package com.railway.service;

import com.railway.exception.RailwayException;
import com.railway.model.RailwayRule;
import com.railway.model.RouteStop;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.TrainSearchResult;
import com.railway.model.enums.TravelClass;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface governing train search, route inquiries, and dynamic fare computation.
 */
public interface TrainService {
    List<Train> getAllActiveTrains() throws RailwayException;
    List<TrainSearchResult> searchTrains(long fromStationId, long toStationId, LocalDate journeyDate) throws RailwayException;
    List<RouteStop> getRouteForTrain(long trainId) throws RailwayException;
    int checkSeatAvailability(long trainId, LocalDate journeyDate, TravelClass travelClass) throws RailwayException;
    BigDecimal calculateFare(Train train, TravelClass travelClass, int distanceKm);
    List<Station> getAllStations() throws RailwayException;
    List<RailwayRule> getAllActiveRules() throws RailwayException;
}
