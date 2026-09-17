package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.response.CustomerStatisticsResponse;
import com.aurainfo.foodapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final UserService userService;

    // =====================================================
    // GET ALL CUSTOMERS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<CustomerStatisticsResponse>> getAllCustomers() {

        return ResponseEntity.ok(
                userService.getAllCustomers()
        );
    }

    // =====================================================
    // GET CUSTOMER DETAILS / STATISTICS
    // =====================================================

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerStatisticsResponse> getCustomerById(
            @PathVariable Long customerId
    ) {

        return ResponseEntity.ok(
                userService.getCustomerStatistics(customerId)
        );
    }
}