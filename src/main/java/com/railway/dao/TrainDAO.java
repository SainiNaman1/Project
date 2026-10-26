package com.railway.dao;

import com.railway.model.Train;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Train fleet entities.
 */
public interface TrainDAO {
    List<Train> findAllActive() throws SQLException;
    Optional<Train> findById(long id) throws SQLException;
    Optional<Train> findByNumber(String trainNumber) throws SQLException;
    List<Train> findTrainsBetweenStations(long sourceStationId, long destStationId) throws SQLException;
    Train save(Train train) throws SQLException;
    void updateStatus(long trainId, boolean active) throws SQLException;
}
