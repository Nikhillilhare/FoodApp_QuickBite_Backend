package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private SalesReportResponse sales;

    private BillingReportResponse billing;

    private StockReportResponse stock;

    private List<DailySalesPointResponse> dailySales;

    private List<TopItemResponse> topItems;

    private List<PaymentMethodBreakdownResponse> paymentMethods;

    private List<CityBreakdownResponse> ordersByCity;
}