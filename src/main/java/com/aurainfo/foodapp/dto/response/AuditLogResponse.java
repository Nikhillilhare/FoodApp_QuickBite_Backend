package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long auditLogId;

    private Long adminId;

    private String adminName;

    private String action;

    private String entityName;

    private Long entityId;

    private LocalDateTime timestamp;
}