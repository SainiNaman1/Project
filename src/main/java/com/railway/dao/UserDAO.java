package com.railway.dao;

import com.railway.model.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findById(long id) throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    User save(User user) throws SQLException;
}
