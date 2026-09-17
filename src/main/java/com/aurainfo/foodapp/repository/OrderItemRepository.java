package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = "product")
    List<OrderItem> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = "product")
    Optional<OrderItem> findByOrderIdAndProductId(
            Long orderId,
            Long productId
    );

    @EntityGraph(attributePaths = "product")
    List<OrderItem> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product","order"})
    Optional<OrderItem> findById(Long id);

//    void deleteByOrderId(Long orderId);

    @Query("""
            SELECT oi.product.id, oi.product.name, COALESCE(SUM(oi.subtotal), 0), COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            WHERE oi.order.orderDate >= :start
              AND oi.order.orderDate < :end
              AND oi.order.orderStatus <> com.aurainfo.foodapp.entity.OrderStatus.CANCELLED
            GROUP BY oi.product.id, oi.product.name
            ORDER BY SUM(oi.subtotal) DESC
            """)
    List<Object[]> findTopItemsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}