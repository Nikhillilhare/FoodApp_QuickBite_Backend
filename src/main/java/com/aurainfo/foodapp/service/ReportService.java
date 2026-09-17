package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.dto.response.BillingReportResponse;
import com.aurainfo.foodapp.dto.response.ReportResponse;
import com.aurainfo.foodapp.dto.response.SalesReportResponse;
import com.aurainfo.foodapp.dto.response.StockReportResponse;
import com.aurainfo.foodapp.entity.ReportRange;

public interface ReportService {

    SalesReportResponse getSalesReport(
            ReportRange range
    );

    BillingReportResponse getBillingReport(
            ReportRange range
    );

    StockReportResponse getStockReport(ReportRange range);

    ReportResponse getCompleteReport(ReportRange range);
}