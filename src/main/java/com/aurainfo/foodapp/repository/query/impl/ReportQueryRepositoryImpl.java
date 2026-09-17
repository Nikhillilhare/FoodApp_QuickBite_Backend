package com.aurainfo.foodapp.repository.query.impl;

import com.aurainfo.foodapp.entity.InventoryTransactionType;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.PaymentStatus;
import com.aurainfo.foodapp.repository.query.ReportQueryRepository;
import com.aurainfo.foodapp.repository.query.ReportQueryRepository.BillingData;
import com.aurainfo.foodapp.repository.query.ReportQueryRepository.SalesData;
import com.aurainfo.foodapp.repository.query.ReportQueryRepository.StockData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public class ReportQueryRepositoryImpl
        implements ReportQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SalesData findSalesData(
            LocalDateTime start,
            LocalDateTime end
    ) {

        Object[] result = entityManager.createQuery(
                        """
                        SELECT
                            COUNT(o.id),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN o.orderStatus = :cancelledStatus
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN o.paymentStatus = :paidStatus
                                         AND o.orderStatus <> :cancelledStatus
                                        THEN o.totalAmount
                                        ELSE 0
                                    END
                                ),
                                0
                            )
        
                        FROM Order o
                        WHERE o.orderDate >= :start
                          AND o.orderDate < :end
                        """,
                        Object[].class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter(
                        "cancelledStatus",
                        OrderStatus.CANCELLED
                )
                .setParameter(
                        "paidStatus",
                        PaymentStatus.PAID
                )
                .getSingleResult();

        return new SalesData(
                toLong(result[0]),
                toLong(result[1]),
                toBigDecimal(result[2])
        );
    }

    @Override
    public BillingData findBillingData(
            LocalDateTime start,
            LocalDateTime end
    ) {

        Object[] result = entityManager.createQuery(
                        """
                        SELECT
                            COUNT(i.id),
                            COALESCE(SUM(i.subtotal), 0),
                            COALESCE(SUM(i.tax), 0),
                            COALESCE(SUM(i.discount), 0),
                            COALESCE(SUM(i.totalAmount), 0)
        
                        FROM Invoice i
                        WHERE i.billingDate >= :start
                          AND i.billingDate < :end
                        """,
                        Object[].class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        return new BillingData(
                toLong(result[0]),
                toBigDecimal(result[1]),
                toBigDecimal(result[2]),
                toBigDecimal(result[3]),
                toBigDecimal(result[4])
        );
    }

    @Override
    public StockData findStockData(
            LocalDateTime start,
            LocalDateTime end
    ) {

        Object[] result = entityManager.createQuery(
                        """
                        SELECT
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :purchase
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :purchase
                                        THEN ABS(t.quantityChanged)
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :sale
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :sale
                                        THEN ABS(t.quantityChanged)
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :adjustment
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :adjustment
                                        THEN t.quantityChanged
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :expired
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(
                                SUM(
                                    CASE
                                        WHEN t.transactionType = :expired
                                        THEN ABS(t.quantityChanged)
                                        ELSE 0
                                    END
                                ),
                                0
                            ),
        
                            COALESCE(SUM(t.quantityChanged), 0)
        
                        FROM InventoryTransaction t
                        WHERE t.createdAt >= :start
                          AND t.createdAt < :end
                        """,
                        Object[].class
                )
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter(
                        "purchase",
                        InventoryTransactionType.PURCHASE_IN
                )
                .setParameter(
                        "sale",
                        InventoryTransactionType.SALE
                )
                .setParameter(
                        "adjustment",
                        InventoryTransactionType.MANUAL_ADJUSTMENT
                )
                .setParameter(
                        "expired",
                        InventoryTransactionType.EXPIRED_REMOVAL
                )
                .getSingleResult();

        return new StockData(
                toLong(result[0]),
                toInt(result[1]),
                toLong(result[2]),
                toInt(result[3]),
                toLong(result[4]),
                toInt(result[5]),
                toLong(result[6]),
                toInt(result[7]),
                toInt(result[8])
        );
    }

    private long toLong(Object value) {
        return value == null
                ? 0L
                : ((Number) value).longValue();
    }

    private int toInt(Object value) {
        return value == null
                ? 0
                : ((Number) value).intValue();
    }

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal decimal) {
            return decimal;
        }

        return new BigDecimal(value.toString());
    }
}