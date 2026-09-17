package com.aurainfo.foodapp.repository.query;

import com.aurainfo.foodapp.dto.response.RecentOrderResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardQueryRepository {

    BigDecimal findSalesBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countOrdersBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countNewCustomersBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countPendingOrders();

    List<RecentOrderResponse> findRecentOrders(int limit);
}