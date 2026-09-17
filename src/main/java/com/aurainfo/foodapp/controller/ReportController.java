package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.response.BillingReportResponse;
import com.aurainfo.foodapp.dto.response.ReportResponse;
import com.aurainfo.foodapp.dto.response.SalesReportResponse;
import com.aurainfo.foodapp.dto.response.StockReportResponse;
import com.aurainfo.foodapp.entity.ReportRange;
import com.aurainfo.foodapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @GetMapping
    public ResponseEntity<ReportResponse> getCompleteReport(
            @RequestParam ReportRange range
    ) {
        return ResponseEntity.ok(
                reportService.getCompleteReport(range)
        );
    }

    // =====================================================
    // SALES REPORT
    // =====================================================

    @GetMapping("/sales")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam ReportRange range
    ) {

        return ResponseEntity.ok(
                reportService.getSalesReport(range)
        );
    }

    // =====================================================
    // BILLING REPORT
    // =====================================================

    @GetMapping("/billing")
    public ResponseEntity<BillingReportResponse> getBillingReport(
            @RequestParam ReportRange range
    ) {

        return ResponseEntity.ok(
                reportService.getBillingReport(range)
        );
    }

    // =====================================================
    // STOCK REPORT
    // =====================================================

    @GetMapping("/stock")
    public ResponseEntity<StockReportResponse> getStockReport(
            @RequestParam ReportRange range
    ) {

        return ResponseEntity.ok(
                reportService.getStockReport(range)
        );
    }
}