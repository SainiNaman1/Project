package com.railway.dao.impl;

import com.railway.dao.StationDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StationDAOImpl implements StationDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public List<Station> findAll() throws SQLException {
        List<Station> stations = new ArrayList<>();
        String sql = "SELECT id, station_code, station_name, city, state FROM stations ORDER BY station_name ASC";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                stations.add(mapRowToStation(rs));
            }
        }
        return stations;
    }

    @Override
    public Optional<Station> findById(long id) throws SQLException {
        String sql = "SELECT id, station_code, station_name, city, state FROM stations WHERE id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStation(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Station> findByCode(String stationCode) throws SQLException {
        String sql = "SELECT id, station_code, station_name, city, state FROM stations WHERE UPPER(station_code) = UPPER(?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, stationCode.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStation(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Station save(Station station) throws SQLException {
        String sql = "INSERT INTO stations (station_code, station_name, city, state) VALUES (?, ?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, station.getStationCode().trim().toUpperCase());
            stmt.setString(2, station.getStationName().trim());
            stmt.setString(3, station.getCity().trim());
            stmt.setString(4, station.getState().trim());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    station.setId(rs.getLong(1));
                }
            }
        }
        return station;
    }

    private Station mapRowToStation(ResultSet rs) throws SQLException {
        return new Station(
                rs.getLong("id"),
                rs.getString("station_code"),
                rs.getString("station_name"),
                rs.getString("city"),
                rs.getString("state")
        );
    }
}
