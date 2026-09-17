package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.AuditLog;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.entity.UserRole;
import com.aurainfo.foodapp.repository.AuditLogRepository;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public AuditLog logAdminAction(
            Long adminId,
            String action,
            String entityName,
            Long entityId
    ) {
        validateId(adminId, "Admin ID");

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Audit action is required");
        }

        if (entityName == null || entityName.isBlank()) {
            throw new IllegalArgumentException(
                    "Entity name is required"
            );
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Admin not found with id: " + adminId
                ));

        AuditLog auditLog = AuditLog.builder()
                .admin(admin)
                .action(action.trim())
                .entityName(entityName.trim())
                .entityId(entityId)
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog logCurrentAdminAction(
            String action,
            String entityName,
            Long entityId
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated admin could not be identified"
            );
        }

        User admin =
                userRepository.findByEmail(
                                authentication.getName()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Authenticated admin not found"
                                )
                        );

        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException(
                    "Only an admin can create an admin audit log"
            );
        }

        if (!Boolean.TRUE.equals(admin.getActive())) {
            throw new IllegalStateException(
                    "Admin account is inactive"
            );
        }

        return logAdminAction(
                admin.getId(),
                action,
                entityName,
                entityId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog getAuditLogById(Long auditLogId) {
        validateId(auditLogId, "Audit Log ID");

        return auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Audit log not found with id: " + auditLogId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByAdmin(Long adminId) {
        validateId(adminId, "Admin ID");

        if (!userRepository.existsById(adminId)) {
            throw new IllegalArgumentException(
                    "Admin not found with id: " + adminId
            );
        }

        return auditLogRepository.findByAdminId(adminId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(
            String entityName,
            Long entityId
    ) {
        if (entityName == null || entityName.isBlank()) {
            throw new IllegalArgumentException(
                    "Entity name is required"
            );
        }

        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException(
                    "Entity ID must be greater than zero"
            );
        }

        return auditLogRepository.findByEntityNameAndEntityId(
                entityName.trim(),
                entityId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action is required"
            );
        }

        return auditLogRepository.findByAction(action.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Start and end timestamps are required"
            );
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "End timestamp cannot be before start timestamp"
            );
        }

        return auditLogRepository.findByTimestampBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLatestLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }
}
