package com.railway.dao.impl;

import com.railway.dao.RouteDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.RouteStop;
import com.railway.model.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RouteDAOImpl implements RouteDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public List<RouteStop> findStopsByTrainId(long trainId) throws SQLException {
        String sql = "SELECT r.id, r.train_id, r.stop_sequence, r.arrival_time, r.departure_time, " +
                     "r.halt_minutes, r.distance_from_origin_km, r.scheduled_platform, " +
                     "s.id AS stn_id, s.station_code, s.station_name, s.city, s.state " +
                     "FROM route_stops r " +
                     "JOIN stations s ON r.station_id = s.id " +
                     "WHERE r.train_id = ? ORDER BY r.stop_sequence ASC";

        List<RouteStop> stops = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stops.add(mapRowToRouteStop(rs));
                }
            }
        }
        return stops;
    }

    @Override
    public Optional<RouteStop> findStop(long trainId, long stationId) throws SQLException {
        String sql = "SELECT r.id, r.train_id, r.stop_sequence, r.arrival_time, r.departure_time, " +
                     "r.halt_minutes, r.distance_from_origin_km, r.scheduled_platform, " +
                     "s.id AS stn_id, s.station_code, s.station_name, s.city, s.state " +
                     "FROM route_stops r " +
                     "JOIN stations s ON r.station_id = s.id " +
                     "WHERE r.train_id = ? AND r.station_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            stmt.setLong(2, stationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRouteStop(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void updatePlatform(long trainId, long stationId, int platformNumber) throws SQLException {
        String sql = "UPDATE route_stops SET scheduled_platform = ? WHERE train_id = ? AND station_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, platformNumber);
            stmt.setLong(2, trainId);
            stmt.setLong(3, stationId);
            stmt.executeUpdate();
        }
    }

    @Override
    public int calculateDistanceKm(long trainId, long fromStationId, long toStationId) throws SQLException {
        String sql = "SELECT (r2.distance_from_origin_km - r1.distance_from_origin_km) AS distance " +
                     "FROM route_stops r1, route_stops r2 " +
                     "WHERE r1.train_id = ? AND r1.station_id = ? " +
                     "  AND r2.train_id = ? AND r2.station_id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, trainId);
            stmt.setLong(2, fromStationId);
            stmt.setLong(3, trainId);
            stmt.setLong(4, toStationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int dist = rs.getInt("distance");
                    return Math.max(dist, 0);
                }
            }
        }
        return 0;
    }

    @Override
    public void saveStops(long trainId, List<RouteStop> stops) throws SQLException {
        String sql = "INSERT INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (RouteStop stop : stops) {
                stmt.setLong(1, trainId);
                stmt.setLong(2, stop.getStation().getId());
                stmt.setInt(3, stop.getStopSequence());
                stmt.setTime(4, stop.getArrivalTime() != null ? Time.valueOf(stop.getArrivalTime()) : null);
                stmt.setTime(5, stop.getDepartureTime() != null ? Time.valueOf(stop.getDepartureTime()) : null);
                stmt.setInt(6, stop.getHaltMinutes());
                stmt.setInt(7, stop.getDistanceFromOriginKm());
                stmt.setInt(8, stop.getScheduledPlatform());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private RouteStop mapRowToRouteStop(ResultSet rs) throws SQLException {
        Station station = new Station(
                rs.getLong("stn_id"),
                rs.getString("station_code"),
                rs.getString("station_name"),
                rs.getString("city"),
                rs.getString("state")
        );

        Time arr = rs.getTime("arrival_time");
        Time dep = rs.getTime("departure_time");

        return new RouteStop(
                rs.getLong("id"),
                rs.getLong("train_id"),
                station,
                rs.getInt("stop_sequence"),
                arr != null ? arr.toLocalTime() : null,
                dep != null ? dep.toLocalTime() : null,
                rs.getInt("halt_minutes"),
                rs.getInt("distance_from_origin_km"),
                rs.getInt("scheduled_platform")
        );
    }
}
