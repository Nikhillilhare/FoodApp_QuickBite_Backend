package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.CancelOrderRequest;
import com.aurainfo.foodapp.dto.request.CheckoutRequest;
import com.aurainfo.foodapp.dto.response.CustomerOrderItemResponse;
import com.aurainfo.foodapp.dto.response.CustomerOrderResponse;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderItem;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.service.OrderItemService;
import com.aurainfo.foodapp.service.OrderService;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserService userService;

    // =====================================================
    // CHECKOUT / PLACE ORDER
    // =====================================================

    @PostMapping("/checkout")
    public ResponseEntity<CustomerOrderResponse> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        Order order =
                orderService.placeOrder(
                        customerId,
                        request.getAddressId()
                );

        return ResponseEntity.ok(
                mapToCustomerOrderResponse(order)
        );
    }

    // =====================================================
    // CUSTOMER ORDER HISTORY
    // =====================================================

    @GetMapping
    public ResponseEntity<List<CustomerOrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        List<Order> orders;

        if (status == null) {

            orders =
                    orderService.getOrdersByCustomer(
                            customerId
                    );

        } else {

            orders =
                    orderService.getOrdersByCustomerAndStatus(
                            customerId,
                            status
                    );
        }

        List<CustomerOrderResponse> response =
                orders.stream()
                        .map(this::mapToCustomerOrderResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // CUSTOMER ORDER DETAIL
    // =====================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<CustomerOrderResponse> getMyOrder(
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

        return ResponseEntity.ok(
                mapToCustomerOrderResponse(order)
        );
    }

    // =====================================================
    // CANCEL ORDER
    // =====================================================

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<CustomerOrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            Authentication authentication
    ) {

        Long customerId =
                getAuthenticatedCustomerId(authentication);

        String cancellationReason =
                request != null
                        ? request.getCancellationReason()
                        : null;

        Order cancelledOrder =
                orderService.cancelOrder(
                        orderId,
                        customerId,
                        cancellationReason
                );

        return ResponseEntity.ok(
                mapToCustomerOrderResponse(
                        cancelledOrder
                )
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
    // ORDER RESPONSE MAPPER
    // =====================================================

    private CustomerOrderResponse mapToCustomerOrderResponse(
            Order order
    ) {

        List<CustomerOrderItemResponse> items =
                orderItemService
                        .getOrderItemsByOrder(order.getId())
                        .stream()
                        .map(this::mapToOrderItemResponse)
                        .toList();

        return CustomerOrderResponse.builder()
                .orderId(order.getId())
                .addressId(
                        order.getAddress() != null
                                ? order.getAddress().getId()
                                : null
                )
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .cancellationReason(
                        order.getCancellationReason()
                )
                .orderDate(order.getOrderDate())
                .items(items)
                .build();
    }

    // =====================================================
    // ORDER ITEM RESPONSE MAPPER
    // =====================================================

    private CustomerOrderItemResponse mapToOrderItemResponse(
            OrderItem item
    ) {

        return CustomerOrderItemResponse.builder()
                .orderItemId(item.getId())
                .productId(
                        item.getProduct().getId()
                )
                .productName(
                        item.getProduct().getName()
                )
                .imageUrl(
                        item.getProduct().getImageUrl()
                )
                .quantity(item.getQuantity())
                .priceAtPurchase(
                        item.getPriceAtPurchase()
                )
                .subtotal(
                        item.getSubtotal()
                )
                .build();
    }
}