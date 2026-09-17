package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    AuditLog logAdminAction(
            Long adminId,
            String action,
            String entityName,
            Long entityId
    );

    AuditLog logCurrentAdminAction(
            String action,
            String entityName,
            Long entityId
    );

    AuditLog getAuditLogById(Long auditLogId);

    List<AuditLog> getLogsByAdmin(Long adminId);

    List<AuditLog> getLogsByEntity(
            String entityName,
            Long entityId
    );

    List<AuditLog> getLogsByAction(String action);

    List<AuditLog> getLogsBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<AuditLog> getLatestLogs();
}
