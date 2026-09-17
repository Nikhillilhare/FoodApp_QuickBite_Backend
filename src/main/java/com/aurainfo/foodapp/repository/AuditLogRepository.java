package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.AuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = "admin")
    List<AuditLog> findByAdminId(Long adminId);

    @EntityGraph(attributePaths = "admin")
    List<AuditLog> findByEntityNameAndEntityId(
            String entityName,
            Long entityId
    );

    @EntityGraph(attributePaths = "admin")
    List<AuditLog> findByAction(String action);

    @EntityGraph(attributePaths = "admin")
    List<AuditLog> findByTimestampBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = "admin")
    List<AuditLog> findAllByOrderByTimestampDesc();
}