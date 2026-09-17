package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.response.AuditLogResponse;
import com.aurainfo.foodapp.entity.AuditLog;
import com.aurainfo.foodapp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    // =====================================================
    // GET ALL AUDIT LOGS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {

        List<AuditLogResponse> response =
                auditLogService.getLatestLogs()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // GET AUDIT LOGS BY ADMIN
    // =====================================================

    @GetMapping("/{adminId}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByAdmin(
            @PathVariable Long adminId
    ) {

        List<AuditLogResponse> response =
                auditLogService.getLogsByAdmin(adminId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private AuditLogResponse mapToResponse(
            AuditLog auditLog
    ) {

        return AuditLogResponse.builder()
                .auditLogId(auditLog.getId())
                .adminId(
                        auditLog.getAdmin() != null
                                ? auditLog.getAdmin().getId()
                                : null
                )
                .adminName(
                        auditLog.getAdmin() != null
                                ? auditLog.getAdmin().getName()
                                : null
                )
                .action(auditLog.getAction())
                .entityName(auditLog.getEntityName())
                .entityId(auditLog.getEntityId())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}