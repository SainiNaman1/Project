package com.railway.dao;

import com.railway.model.RouteStop;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for train route stops and platform schedules.
 */
public interface RouteDAO {
    List<RouteStop> findStopsByTrainId(long trainId) throws SQLException;
    Optional<RouteStop> findStop(long trainId, long stationId) throws SQLException;
    void updatePlatform(long trainId, long stationId, int platformNumber) throws SQLException;
    int calculateDistanceKm(long trainId, long fromStationId, long toStationId) throws SQLException;
    void saveStops(long trainId, List<RouteStop> stops) throws SQLException;
}
