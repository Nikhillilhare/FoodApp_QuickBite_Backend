package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order-items")
@RequiredArgsConstructor
public class AdminOrderItemController {

    private final OrderItemService orderItemService;

    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<String> deleteOrderItem(
            @PathVariable Long orderItemId
    ) {

        orderItemService.deleteOrderItem(orderItemId);

        return ResponseEntity.ok(
                "Order item deleted successfully"
        );
    }
}