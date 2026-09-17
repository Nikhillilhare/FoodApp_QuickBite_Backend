package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    Optional<Invoice> findByOrderId(Long orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);
    boolean existsByOrderId(Long orderId);

    long countByBillingDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(i.subtotal), 0)
        FROM Invoice i
        WHERE i.billingDate >= :start
          AND i.billingDate < :end
        """)
    BigDecimal sumSubtotalBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(i.tax), 0)
        FROM Invoice i
        WHERE i.billingDate >= :start
          AND i.billingDate < :end
        """)
    BigDecimal sumTaxBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(i.discount), 0)
        FROM Invoice i
        WHERE i.billingDate >= :start
          AND i.billingDate < :end
        """)
    BigDecimal sumDiscountBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount), 0)
        FROM Invoice i
        WHERE i.billingDate >= :start
          AND i.billingDate < :end
        """)
    BigDecimal sumTotalAmountBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
