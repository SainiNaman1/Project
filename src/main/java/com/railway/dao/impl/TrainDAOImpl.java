package com.railway.dao.impl;

import com.railway.dao.TrainDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.MailExpressTrain;
import com.railway.model.RajdhaniExpress;
import com.railway.model.ShatabdiExpress;
import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.enums.TrainType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainDAOImpl implements TrainDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public List<Train> findAllActive() throws SQLException {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT t.id, t.train_number, t.train_name, t.train_type, t.base_fare_per_km, t.active, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM trains t " +
                     "JOIN stations s1 ON t.source_station_id = s1.id " +
                     "JOIN stations s2 ON t.dest_station_id = s2.id " +
                     "WHERE t.active = TRUE ORDER BY t.train_number ASC";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                trains.add(mapRowToTrain(rs));
            }
        }
        return trains;
    }

    @Override
    public Optional<Train> findById(long id) throws SQLException {
        String sql = "SELECT t.id, t.train_number, t.train_name, t.train_type, t.base_fare_per_km, t.active, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM trains t " +
                     "JOIN stations s1 ON t.source_station_id = s1.id " +
                     "JOIN stations s2 ON t.dest_station_id = s2.id " +
                     "WHERE t.id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTrain(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Train> findByNumber(String trainNumber) throws SQLException {
        String sql = "SELECT t.id, t.train_number, t.train_name, t.train_type, t.base_fare_per_km, t.active, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM trains t " +
                     "JOIN stations s1 ON t.source_station_id = s1.id " +
                     "JOIN stations s2 ON t.dest_station_id = s2.id " +
                     "WHERE t.train_number = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trainNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTrain(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Train> findTrainsBetweenStations(long sourceStationId, long destStationId) throws SQLException {
        // Query returns active trains that stop at both stations where the source stop sequence < dest stop sequence
        String sql = "SELECT DISTINCT t.id, t.train_number, t.train_name, t.train_type, t.base_fare_per_km, t.active, " +
                     "s1.id AS src_id, s1.station_code AS src_code, s1.station_name AS src_name, s1.city AS src_city, s1.state AS src_state, " +
                     "s2.id AS dst_id, s2.station_code AS dst_code, s2.station_name AS dst_name, s2.city AS dst_city, s2.state AS dst_state " +
                     "FROM trains t " +
                     "JOIN stations s1 ON t.source_station_id = s1.id " +
                     "JOIN stations s2 ON t.dest_station_id = s2.id " +
                     "JOIN route_stops r1 ON t.id = r1.train_id AND r1.station_id = ? " +
                     "JOIN route_stops r2 ON t.id = r2.train_id AND r2.station_id = ? " +
                     "WHERE t.active = TRUE AND r1.stop_sequence < r2.stop_sequence " +
                     "ORDER BY t.train_number ASC";

        List<Train> trains = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sourceStationId);
            stmt.setLong(2, destStationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    trains.add(mapRowToTrain(rs));
                }
            }
        }
        return trains;
    }

    @Override
    public Train save(Train train) throws SQLException {
        String sql = "INSERT INTO trains (train_number, train_name, train_type, source_station_id, dest_station_id, base_fare_per_km, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, train.getTrainNumber().trim());
            stmt.setString(2, train.getTrainName().trim());
            stmt.setString(3, train.getTrainType().name());
            stmt.setLong(4, train.getSourceStation().getId());
            stmt.setLong(5, train.getDestStation().getId());
            stmt.setBigDecimal(6, train.getBaseFarePerKm());
            stmt.setBoolean(7, train.isActive());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    train.setId(rs.getLong(1));
                }
            }
        }
        return train;
    }

    @Override
    public void updateStatus(long trainId, boolean active) throws SQLException {
        String sql = "UPDATE trains SET active = ? WHERE id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setLong(2, trainId);
            stmt.executeUpdate();
        }
    }

    /**
     * Factory Method: Maps database row to polymorphic Train subclass.
     */
    private Train mapRowToTrain(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String number = rs.getString("train_number");
        String name = rs.getString("train_name");
        TrainType type = TrainType.valueOf(rs.getString("train_type"));
        BigDecimal baseFare = rs.getBigDecimal("base_fare_per_km");
        boolean active = rs.getBoolean("active");

        Station src = new Station(
                rs.getLong("src_id"),
                rs.getString("src_code"),
                rs.getString("src_name"),
                rs.getString("src_city"),
                rs.getString("src_state")
        );

        Station dst = new Station(
                rs.getLong("dst_id"),
                rs.getString("dst_code"),
                rs.getString("dst_name"),
                rs.getString("dst_city"),
                rs.getString("dst_state")
        );

        // Polymorphic instantiation based on type
        return switch (type) {
            case RAJDHANI -> new RajdhaniExpress(id, number, name, src, dst, baseFare, active);
            case SHATABDI -> new ShatabdiExpress(id, number, name, src, dst, baseFare, active);
            default -> new MailExpressTrain(id, number, name, src, dst, baseFare, active);
        };
    }
}
