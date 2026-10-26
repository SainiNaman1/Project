package com.railway.dao;

import com.railway.model.Station;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Station entities.
 */
public interface StationDAO {
    List<Station> findAll() throws SQLException;
    Optional<Station> findById(long id) throws SQLException;
    Optional<Station> findByCode(String stationCode) throws SQLException;
    Station save(Station station) throws SQLException;
}
