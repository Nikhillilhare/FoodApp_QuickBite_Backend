package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findByCustomerIdOrderByOrderDateDesc(
            Long customerId
    );

    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findByCustomerIdAndOrderStatusOrderByOrderDateDesc(
            Long customerId,
            OrderStatus orderStatus
    );

    List<Order> findByOrderStatus(
            OrderStatus orderStatus
    );

//    List<Order> findByPaymentStatus(
//            PaymentStatus paymentStatus
//    );

    @EntityGraph(attributePaths = {"customer", "address"})
    Optional<Order> findById(Long id);

    long countByOrderDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByOrderStatusAndOrderDateBetween(
            OrderStatus orderStatus,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM Order o
            WHERE o.orderDate >= :start
              AND o.orderDate < :end
              AND o.paymentStatus = :paymentStatus
              AND o.orderStatus <> :cancelledStatus
            """)
    BigDecimal sumSalesBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("paymentStatus")
            PaymentStatus paymentStatus,
            @Param("cancelledStatus")
            OrderStatus cancelledStatus
    );

    @Query("""
        SELECT COUNT(o.id)
        FROM Order o
        WHERE o.orderDate >= :start
          AND o.orderDate < :end
          AND o.paymentStatus = :paymentStatus
          AND o.orderStatus <> :cancelledStatus
        """)
    long countSuccessfulOrdersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("cancelledStatus") OrderStatus cancelledStatus
    );

    List<Order> findTop10ByOrderByOrderDateDesc();

    List<Order> findByOrderDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByOrderStatus(
            OrderStatus orderStatus
    );

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByOrderStatusOrderByOrderDateDesc(
            OrderStatus status
    );

    long countByPaymentStatusAndOrderDateBetween(
            PaymentStatus paymentStatus,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT COALESCE(o.address.city, 'Unknown'), COUNT(o.id), COALESCE(SUM(o.totalAmount), 0)
            FROM Order o
            WHERE o.orderDate >= :start
              AND o.orderDate < :end
              AND o.orderStatus <> :cancelledStatus
            GROUP BY o.address.city
            ORDER BY SUM(o.totalAmount) DESC
            """)
    List<Object[]> findCityBreakdownBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("cancelledStatus") OrderStatus cancelledStatus
    );

    @Query(value = """
            SELECT DAYOFWEEK(order_date) AS dow, COALESCE(SUM(total_amount), 0), COUNT(*)
            FROM orders
            WHERE order_date >= :start
              AND order_date < :end
              AND order_status <> 'CANCELLED'
            GROUP BY DAYOFWEEK(order_date)
            """, nativeQuery = true)
    List<Object[]> findDailySalesBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}