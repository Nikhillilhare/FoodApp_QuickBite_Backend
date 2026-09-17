package com.aurainfo.foodapp.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private BigDecimal todaySales;
    private long todayOrders;
    private long newCustomers;
    private long pendingOrders;
    private List<RecentOrderResponse> recentOrders;
}
