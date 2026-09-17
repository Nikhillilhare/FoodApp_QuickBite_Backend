package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.response.InvoiceResponse;
import com.aurainfo.foodapp.entity.Invoice;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.service.InvoiceService;
import com.aurainfo.foodapp.service.OrderService;
import com.aurainfo.foodapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final OrderService orderService;
    private final UserService userService;

    // =====================================================
    // CUSTOMER - GET MY ORDER INVOICE
    // =====================================================

    @GetMapping("/api/customer/orders/{orderId}/invoice")
    public ResponseEntity<InvoiceResponse> getCustomerInvoice(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedUserId(authentication);

        Order order =
                orderService.getOrderById(orderId);

        validateOrderOwnership(order, customerId);

        Invoice invoice =
                invoiceService.getInvoiceByOrderId(orderId);

        return ResponseEntity.ok(
                mapToResponse(invoice)
        );
    }

    // =====================================================
    // ADMIN - GET ORDER INVOICE
    // =====================================================

    @GetMapping("/api/admin/orders/{orderId}/invoice")
    public ResponseEntity<InvoiceResponse> getAdminInvoice(
            @PathVariable Long orderId
    ) {

        // Validate that the order exists.
        orderService.getOrderById(orderId);

        Invoice invoice =
                invoiceService.getInvoiceByOrderId(orderId);

        return ResponseEntity.ok(
                mapToResponse(invoice)
        );
    }

    // =====================================================
    // CUSTOMER AUTHENTICATION
    // =====================================================

    private Long getAuthenticatedUserId(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated customer could not be identified"
            );
        }

        User user =
                userService.getUserByEmail(
                        authentication.getName()
                );

        return user.getId();
    }

    // =====================================================
    // ORDER OWNERSHIP
    // =====================================================

    private void validateOrderOwnership(
            Order order,
            Long customerId
    ) {

        if (order.getCustomer() == null ||
                order.getCustomer().getId() == null ||
                !order.getCustomer()
                        .getId()
                        .equals(customerId)) {

            throw new IllegalArgumentException(
                    "Order does not belong to this customer"
            );
        }
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private InvoiceResponse mapToResponse(
            Invoice invoice
    ) {

        return InvoiceResponse.builder()
                .invoiceId(invoice.getId())
                .orderId(invoice.getOrder().getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .billingDate(invoice.getBillingDate())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .discount(invoice.getDiscount())
                .totalAmount(invoice.getTotalAmount())
                .build();
    }
}