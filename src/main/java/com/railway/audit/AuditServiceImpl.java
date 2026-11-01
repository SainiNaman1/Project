package com.railway.audit;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class AuditServiceImpl implements AuditService {

    private final AuditLogDAO auditLogDAO;

    public AuditServiceImpl() {
        this(new AuditLogDAOImpl());
    }

    public AuditServiceImpl(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    @Override
    public void recordAction(Long userId, String action, String entityType, String entityId, String details) {
        try {
            AuditLog log = new AuditLog(0L, userId, action, entityType, entityId, details, LocalDateTime.now());
            auditLogDAO.log(log);
        } catch (SQLException e) {
            System.err.println("[AuditService] Warning: Failed to record audit log: " + e.getMessage());
        }
    }

    @Override
    public List<AuditLog> getRecentLogs(int limit) {
        try {
            return auditLogDAO.findRecent(limit);
        } catch (SQLException e) {
            System.err.println("[AuditService] Warning: Failed to fetch recent audit logs: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
