package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.UpdateOrderStatusRequest;
import com.aurainfo.foodapp.dto.response.AdminOrderResponse;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.service.OrderItemService;
import com.aurainfo.foodapp.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // =====================================================
    // GET ALL ORDERS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status
    ) {

        return ResponseEntity.ok(
                orderService.getAllAdminOrders(status)
        );
    }

    // =====================================================
    // GET ORDER BY ID
    // =====================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderResponse> getOrderById(
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                orderService.getAdminOrderById(orderId)
        );
    }

    // =====================================================
    // UPDATE ORDER STATUS
    // =====================================================

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Order updatedOrder = orderService.updateOrderStatus(
                orderId,
                request.getStatus(),
                request.getCancellationReason()
        );

        return ResponseEntity.ok(
                orderService.getAdminOrderById(updatedOrder.getId())
        );
    }
}