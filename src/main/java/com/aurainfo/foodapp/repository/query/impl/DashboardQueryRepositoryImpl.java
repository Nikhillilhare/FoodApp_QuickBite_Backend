package com.aurainfo.foodapp.repository.query.impl;

import com.aurainfo.foodapp.dto.response.RecentOrderResponse;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.PaymentStatus;
import com.aurainfo.foodapp.entity.UserRole;
import com.aurainfo.foodapp.repository.query.DashboardQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DashboardQueryRepositoryImpl
        implements DashboardQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BigDecimal findSalesBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {

        return entityManager.createQuery(
                        """
                        SELECT COALESCE(SUM(o.totalAmount), 0)
                        FROM Order o
                        WHERE o.orderDate >= :start
                          AND o.orderDate < :end
                          AND o.paymentStatus = :paidStatus
                          AND o.orderStatus <> :cancelledStatus
                        """,
                        BigDecimal.class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("paidStatus", PaymentStatus.PAID)
                .setParameter("cancelledStatus", OrderStatus.CANCELLED)
                .getSingleResult();
    }

    @Override
    public long countOrdersBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(o.id)
                        FROM Order o
                        WHERE o.orderDate >= :start
                          AND o.orderDate < :end
                        """,
                        Long.class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    @Override
    public long countNewCustomersBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(u.id)
                        FROM User u
                        WHERE u.createdAt >= :start
                          AND u.createdAt < :end
                          AND u.role = :role
                        """,
                        Long.class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("role", UserRole.CUSTOMER)
                .getSingleResult();
    }

    @Override
    public long countPendingOrders() {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(o.id)
                        FROM Order o
                        WHERE o.orderStatus = :status
                        """,
                        Long.class
                )
                .setParameter("status", OrderStatus.PENDING)
                .getSingleResult();
    }

    @Override
    public List<RecentOrderResponse> findRecentOrders(
            int limit
    ) {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Recent order limit must be greater than zero"
            );
        }

        return entityManager.createQuery(
                        """
                        SELECT new com.aurainfo.foodapp.dto.response.RecentOrderResponse(
                            o.id,
                            o.customer.name,
                            o.totalAmount,
                            o.orderStatus,
                            o.orderDate
                        )
                        FROM Order o
                        ORDER BY o.orderDate DESC
                        """,
                        RecentOrderResponse.class
                )
                .setMaxResults(limit)
                .getResultList();
    }
}
