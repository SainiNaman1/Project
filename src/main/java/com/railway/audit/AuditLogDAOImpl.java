package com.railway.audit;

import com.railway.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    private final DatabaseConnection database = DatabaseConnection.getInstance();

    @Override
    public void log(AuditLog auditLog) throws SQLException {
        try (Connection conn = database.getConnection()) {
            log(conn, auditLog);
        }
    }

    @Override
    public void log(Connection conn, AuditLog auditLog) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (auditLog.getUserId() != null) {
                stmt.setLong(1, auditLog.getUserId());
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
            }
            stmt.setString(2, auditLog.getAction());
            stmt.setString(3, auditLog.getEntityType());
            stmt.setString(4, auditLog.getEntityId());
            stmt.setString(5, auditLog.getDetails());
            stmt.setTimestamp(6, Timestamp.valueOf(auditLog.getTimestamp() != null ? auditLog.getTimestamp() : LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT id, user_id, action, entity_type, entity_id, details, timestamp FROM audit_logs ORDER BY id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    Long userId = rs.getObject("user_id") != null ? rs.getLong("user_id") : null;
                    String action = rs.getString("action");
                    String entityType = rs.getString("entity_type");
                    String entityId = rs.getString("entity_id");
                    String details = rs.getString("details");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    list.add(new AuditLog(id, userId, action, entityType, entityId, details, ts != null ? ts.toLocalDateTime() : null));
                }
            }
        }
        return list;
    }
}
