package com.railway.dao;

import com.railway.model.RailwayRule;
import java.sql.SQLException;
import java.util.List;

public interface RailwayRuleDAO {
    List<RailwayRule> findAllActive() throws SQLException;
    RailwayRule save(RailwayRule rule) throws SQLException;
}
