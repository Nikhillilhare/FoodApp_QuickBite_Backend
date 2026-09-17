package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.Payment;
import com.aurainfo.foodapp.entity.PaymentTransactionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = "order")
    Optional<Payment> findById(Long id);

    @EntityGraph(attributePaths = "order")
    List<Payment> findByOrderIdOrderByIdDesc(Long orderId);

    List<Payment> findByStatus(PaymentTransactionStatus status);

    Optional<Payment> findByTransactionRef(String transactionRef);

    boolean existsByTransactionRef(String transactionRef);

    @Query("""
            SELECT p.paymentMethod, COUNT(p.id), COALESCE(SUM(p.order.totalAmount), 0)
            FROM Payment p
            WHERE p.order.orderDate >= :start
              AND p.order.orderDate < :end
              AND p.status = com.aurainfo.foodapp.entity.PaymentTransactionStatus.SUCCESS
            GROUP BY p.paymentMethod
            ORDER BY SUM(p.order.totalAmount) DESC
            """)
    List<Object[]> findPaymentMethodBreakdownBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}