package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.InventoryTransaction;

import com.aurainfo.foodapp.entity.InventoryTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;


public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction,Long> {
    List<InventoryTransaction> findByProductId(Long productId);
    // =====================================================
    // HISTORY WITH PRODUCT + USER LOADED
    // =====================================================

    @Query("""
            SELECT t
            FROM InventoryTransaction t
            JOIN FETCH t.product p
            LEFT JOIN FETCH t.performedBy u
            WHERE p.id = :productId
            ORDER BY t.createdAt DESC
            """)
    List<InventoryTransaction> findHistoryByProductId(
            @Param("productId") Long productId
    );

//    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryTransaction> findByTransactionType(
            InventoryTransactionType transactionType
    );

    List<InventoryTransaction> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end);

    List<InventoryTransaction> findByTransactionTypeAndCreatedAtBetween(
            InventoryTransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    );
}
