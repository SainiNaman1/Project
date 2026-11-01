package com.railway.audit;

import java.util.List;

public interface AuditService {
    void recordAction(Long userId, String action, String entityType, String entityId, String details);
    List<AuditLog> getRecentLogs(int limit);
}
