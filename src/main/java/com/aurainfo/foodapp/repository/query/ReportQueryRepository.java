package com.aurainfo.foodapp.repository.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ReportQueryRepository {

    SalesData findSalesData(
            LocalDateTime start,
            LocalDateTime end
    );

    BillingData findBillingData(
            LocalDateTime start,
            LocalDateTime end
    );

    StockData findStockData(
            LocalDateTime start,
            LocalDateTime end
    );

    record SalesData(
            long totalOrders,
            long cancelledOrders,
            BigDecimal totalSales
    ) {
    }

    record BillingData(
            long invoiceCount,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal discount,
            BigDecimal totalAmount
    ) {
    }

    record StockData(
            long purchaseTransactions,
            int purchasedQuantity,
            long saleTransactions,
            int soldQuantity,
            long manualAdjustmentTransactions,
            int manualAdjustmentQuantity,
            long expiredRemovalTransactions,
            int expiredRemovalQuantity,
            int netStockChange
    ) {
    }
}