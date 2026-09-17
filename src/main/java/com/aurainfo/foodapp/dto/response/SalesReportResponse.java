package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportResponse {

    private String range;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private long totalOrders;

    private long cancelledOrders;

    private BigDecimal totalSales;

    private BigDecimal averageOrderValue;

    private double refundRate;
}