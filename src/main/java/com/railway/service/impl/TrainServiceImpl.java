package com.railway.service.impl;

import com.railway.dao.RailwayRuleDAO;
import com.railway.dao.RouteDAO;
import com.railway.dao.SeatDAO;
import com.railway.dao.StationDAO;
import com.railway.dao.TrainDAO;
import com.railway.dao.impl.RailwayRuleDAOImpl;
import com.railway.dao.impl.RouteDAOImpl;
import com.railway.dao.impl.SeatDAOImpl;
import com.railway.dao.impl.StationDAOImpl;
import com.railway.dao.impl.TrainDAOImpl;
import com.railway.exception.RailwayException;
import com.railway.exception.ValidationException;
import com.railway.model.RailwayRule;
import com.railway.model.RouteStop;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.dto.TrainSearchResult;
import com.railway.model.enums.TravelClass;
import com.railway.service.TrainService;
import com.railway.util.FareCalculator;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainServiceImpl implements TrainService {

    private final TrainDAO trainDAO;
    private final RouteDAO routeDAO;
    private final SeatDAO seatDAO;
    private final StationDAO stationDAO;
    private final RailwayRuleDAO ruleDAO;

    public TrainServiceImpl() {
        this.trainDAO = new TrainDAOImpl();
        this.routeDAO = new RouteDAOImpl();
        this.seatDAO = new SeatDAOImpl();
        this.stationDAO = new StationDAOImpl();
        this.ruleDAO = new RailwayRuleDAOImpl();
    }

    public TrainServiceImpl(TrainDAO trainDAO, RouteDAO routeDAO, SeatDAO seatDAO, StationDAO stationDAO, RailwayRuleDAO ruleDAO) {
        this.trainDAO = trainDAO;
        this.routeDAO = routeDAO;
        this.seatDAO = seatDAO;
        this.stationDAO = stationDAO;
        this.ruleDAO = ruleDAO;
    }

    @Override
    public List<Train> getAllActiveTrains() throws RailwayException {
        try {
            return trainDAO.findAllActive();
        } catch (SQLException e) {
            throw new RailwayException("Error fetching train fleet: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TrainSearchResult> searchTrains(long fromStationId, long toStationId, LocalDate journeyDate) throws RailwayException {
        if (fromStationId == toStationId) {
            throw new ValidationException("Boarding and destination stations cannot be the same.");
        }
        if (journeyDate == null || journeyDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Journey date cannot be in the past. Please select today or a future date.");
        }

        try {
            List<Train> matchingTrains = trainDAO.findTrainsBetweenStations(fromStationId, toStationId);
            List<TrainSearchResult> results = new ArrayList<>();

            for (Train train : matchingTrains) {
                Optional<RouteStop> srcStopOpt = routeDAO.findStop(train.getId(), fromStationId);
                Optional<RouteStop> dstStopOpt = routeDAO.findStop(train.getId(), toStationId);

                if (srcStopOpt.isPresent() && dstStopOpt.isPresent()) {
                    RouteStop srcStop = srcStopOpt.get();
                    RouteStop dstStop = dstStopOpt.get();

                    if (srcStop.getStopSequence() < dstStop.getStopSequence()) {
                        int distanceKm = Math.max(dstStop.getDistanceFromOriginKm() - srcStop.getDistanceFromOriginKm(), 50);

                        TrainSearchResult result = new TrainSearchResult(train, srcStop, dstStop, distanceKm);

                        // Populate seat availability and fares across travel classes
                        for (TravelClass tc : TravelClass.values()) {
                            int available = seatDAO.getAvailableSeatCount(train.getId(), journeyDate, tc);
                            result.getAvailableSeats().put(tc, available);

                            BigDecimal fare = FareCalculator.calculateSinglePassengerFare(train, tc, distanceKm);
                            result.getEstimatedFares().put(tc, fare);
                        }
                        results.add(result);
                    }
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RailwayException("Search failed due to database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RouteStop> getRouteForTrain(long trainId) throws RailwayException {
        try {
            return routeDAO.findStopsByTrainId(trainId);
        } catch (SQLException e) {
            throw new RailwayException("Error fetching train itinerary: " + e.getMessage(), e);
        }
    }

    @Override
    public int checkSeatAvailability(long trainId, LocalDate journeyDate, TravelClass travelClass) throws RailwayException {
        try {
            return seatDAO.getAvailableSeatCount(trainId, journeyDate, travelClass);
        } catch (SQLException e) {
            throw new RailwayException("Error checking seat count: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal calculateFare(Train train, TravelClass travelClass, int distanceKm) {
        return FareCalculator.calculateSinglePassengerFare(train, travelClass, distanceKm);
    }

    @Override
    public List<Station> getAllStations() throws RailwayException {
        try {
            return stationDAO.findAll();
        } catch (SQLException e) {
            throw new RailwayException("Error loading stations: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RailwayRule> getAllActiveRules() throws RailwayException {
        try {
            return ruleDAO.findAllActive();
        } catch (SQLException e) {
            throw new RailwayException("Error retrieving railway rules: " + e.getMessage(), e);
        }
    }
}
