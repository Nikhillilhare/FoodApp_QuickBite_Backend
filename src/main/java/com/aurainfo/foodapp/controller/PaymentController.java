package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.CreatePaymentRequest;
import com.aurainfo.foodapp.dto.response.PaymentResponse;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.Payment;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.service.OrderService;
import com.aurainfo.foodapp.service.PaymentService;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;

    // =====================================================
    // CREATE / INITIATE PAYMENT
    // =====================================================

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        Order order =
                orderService.getOrderById(orderId);

        validateOrderOwnership(
                order,
                customerId
        );

        Payment payment =
                paymentService.createPayment(
                        orderId,
                        request.getPaymentMethod(),
                        request.getTransactionRef()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(payment));
    }

    // =====================================================
    // GET PAYMENT STATUS
    // =====================================================

    @GetMapping("/{orderId}/payment")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        Order order =
                orderService.getOrderById(orderId);

        validateOrderOwnership(
                order,
                customerId
        );

        List<Payment> payments =
                paymentService.getPaymentsByOrder(orderId);

        if (payments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Payment not found for order id: "
                            + orderId
            );
        }

        // Repository returns newest payment first
        Payment latestPayment = payments.get(0);

        return ResponseEntity.ok(
                mapToResponse(latestPayment)
        );
    }

    // =====================================================
    // AUTHENTICATED CUSTOMER
    // =====================================================

    private Long getAuthenticatedCustomerId(
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

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated customer could not be found"
            );
        }

        return user.getId();
    }

    // =====================================================
    // ORDER OWNERSHIP
    // =====================================================

    private void validateOrderOwnership(
            Order order,
            Long customerId
    ) {

        if (order == null ||
                order.getCustomer() == null ||
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

    private PaymentResponse mapToResponse(
            Payment payment
    ) {

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(
                        payment.getOrder().getId()
                )
                .paymentMethod(
                        payment.getPaymentMethod()
                )
                .transactionRef(
                        payment.getTransactionRef()
                )
                .status(
                        payment.getStatus()
                )
                .paidAt(
                        payment.getPaidAt()
                )
                .build();
    }
}