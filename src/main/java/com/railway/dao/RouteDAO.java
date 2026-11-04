package com.railway.dao;

import com.railway.model.RouteStop;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RouteDAO {
    List<RouteStop> findStopsByTrainId(long trainId) throws SQLException;
    default List<RouteStop> findStopsByTrainId(Connection conn, long trainId) throws SQLException {
        return findStopsByTrainId(trainId);
    }

    Optional<RouteStop> findStop(long trainId, long stationId) throws SQLException;
    default Optional<RouteStop> findStop(Connection conn, long trainId, long stationId) throws SQLException {
        return findStop(trainId, stationId);
    }

    void updatePlatform(long trainId, long stationId, int platformNumber) throws SQLException;
    default void updatePlatform(Connection conn, long trainId, long stationId, int platformNumber) throws SQLException {
        updatePlatform(trainId, stationId, platformNumber);
    }

    int calculateDistanceKm(long trainId, long fromStationId, long toStationId) throws SQLException;
    default int calculateDistanceKm(Connection conn, long trainId, long fromStationId, long toStationId) throws SQLException {
        return calculateDistanceKm(trainId, fromStationId, toStationId);
    }

    void saveStops(long trainId, List<RouteStop> stops) throws SQLException;
    default void saveStops(Connection conn, long trainId, List<RouteStop> stops) throws SQLException {
        saveStops(trainId, stops);
    }
}
