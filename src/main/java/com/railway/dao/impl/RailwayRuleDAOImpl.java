package com.railway.dao.impl;

import com.railway.dao.RailwayRuleDAO;
import com.railway.database.DatabaseConnection;
import com.railway.model.RailwayRule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RailwayRuleDAOImpl implements RailwayRuleDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public List<RailwayRule> findAllActive() throws SQLException {
        String sql = "SELECT id, category, title, content, active FROM railway_rules WHERE active = TRUE ORDER BY category ASC, id ASC";
        List<RailwayRule> rules = new ArrayList<>();

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rules.add(new RailwayRule(
                        rs.getLong("id"),
                        rs.getString("category"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getBoolean("active")
                ));
            }
        }
        return rules;
    }

    @Override
    public RailwayRule save(RailwayRule rule) throws SQLException {
        String sql = "INSERT INTO railway_rules (category, title, content, active) VALUES (?, ?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, rule.getCategory().trim());
            stmt.setString(2, rule.getTitle().trim());
            stmt.setString(3, rule.getContent().trim());
            stmt.setBoolean(4, rule.isActive());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    rule.setId(rs.getLong(1));
                }
            }
        }
        return rule;
    }
}
