package com.railway.audit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AuditLogDAO {
    void log(AuditLog auditLog) throws SQLException;
    void log(Connection conn, AuditLog auditLog) throws SQLException;
    List<AuditLog> findRecent(int limit) throws SQLException;
}
