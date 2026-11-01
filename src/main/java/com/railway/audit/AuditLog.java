package com.railway.audit;

import java.time.LocalDateTime;

/**
 * Domain entity capturing an auditable system event or administrative action.
 */
public class AuditLog {
    private long id;
    private Long userId;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog() {
    }

    public AuditLog(long id, Long userId, String action, String entityType, String entityId, String details, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
