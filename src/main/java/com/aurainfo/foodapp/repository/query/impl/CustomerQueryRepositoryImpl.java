package com.aurainfo.foodapp.repository.query.impl;

import com.aurainfo.foodapp.dto.response.CustomerStatisticsResponse;
import com.aurainfo.foodapp.entity.UserRole;
import com.aurainfo.foodapp.repository.query.CustomerQueryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerQueryRepositoryImpl
        implements CustomerQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<CustomerStatisticsResponse> findAllCustomerStatistics() {

        return entityManager.createQuery(
                        customerStatisticsQuery(""),
                        CustomerStatisticsResponse.class
                )
                .setParameter("role", UserRole.CUSTOMER)
                .getResultList();
    }

    @Override
    public Optional<CustomerStatisticsResponse> findCustomerStatisticsById(
            Long customerId
    ) {

        return entityManager.createQuery(
                        customerStatisticsQuery(
                                " AND u.id = :customerId"
                        ),
                        CustomerStatisticsResponse.class
                )
                .setParameter("role", UserRole.CUSTOMER)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst();
    }

    private String customerStatisticsQuery(
            String additionalCondition
    ) {

        return """
                SELECT new com.aurainfo.foodapp.dto.response.CustomerStatisticsResponse(
                    u.id,
                    u.name,
                    u.email,
                    u.phone,
                    a.city,
                    COUNT(o.id),
                    COALESCE(
                        SUM(
                            CASE
                                WHEN o.paymentStatus =
                                     com.aurainfo.foodapp.entity.PaymentStatus.PAID
                                 AND o.orderStatus <>
                                     com.aurainfo.foodapp.entity.OrderStatus.CANCELLED
                                THEN o.totalAmount
                                ELSE 0
                            END
                        ),
                        0
                    ),
                    u.createdAt,
                    u.active
                )
                FROM User u
                LEFT JOIN Address a
                    ON a.user.id = u.id
                   AND a.defaultAddress = true
                LEFT JOIN Order o
                    ON o.customer.id = u.id
                WHERE u.role = :role
                """ + additionalCondition + """
                GROUP BY
                    u.id,
                    u.name,
                    u.email,
                    u.phone,
                    a.city,
                    u.createdAt,
                    u.active
                ORDER BY u.createdAt DESC
                """;
    }
}