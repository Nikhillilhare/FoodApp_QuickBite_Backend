package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.dto.response.AdminOrderResponse;
import com.aurainfo.foodapp.entity.*;
import com.aurainfo.foodapp.repository.*;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.InventoryService;
import com.aurainfo.foodapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;

    private void validateProductForOrder(Product product){
        if (product == null){
            throw new IllegalStateException("Cart contains an Invalid Product");
        }
        if (product.getStatus() == null || product.getStatus() != ProductStatus.ACTIVE){
            throw new IllegalStateException(
                    "Product is Inactive: "
                            +product.getId()
            );
        }

        if (product.getPrice() == null ||
                product.getPrice().signum() < 0) {

            throw new IllegalStateException(
                    "Invalid product price for product: "
                            + product.getId()
            );
        }

        if (product.getStockQuantity() < 0) {
            throw new IllegalStateException(
                    "Product stock cannot be negative"
            );
        }
    }
    private void validateId(
            Long id,
            String fieldName
    ) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }

    private boolean isValidStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        return switch (currentStatus) {

            case PENDING -> newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED -> newStatus == OrderStatus.PREPARING
                            || newStatus == OrderStatus.CANCELLED;

            case PREPARING -> newStatus == OrderStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY -> newStatus == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };
    }

    @Override
    public Order placeOrder(Long customerId, Long addressId) {
        validateId(customerId,"Customer ID");
        validateId(addressId,"Address ID");

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with id: "
                                        + customerId
                        )
                );

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found with id: "
                                        + addressId
                        )
                );
        if (!address.getUser().getId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "Address does not belong to this customer"
            );
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Cart not found for customer id: "
                                        + customerId
                        )
                );

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot place order because cart is empty"
            );
        }

        /*
         * First calculate the order total from the current
         * product prices.
         */
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            validateProductForOrder(product);

            int requestedQuantity = cartItem.getQuantity();

            if (requestedQuantity <= 0) {
                throw new IllegalStateException(
                        "Invalid cart quantity for product: "
                                + product.getId()
                );
            }

            if (requestedQuantity >
                    product.getStockQuantity()) {

                throw new IllegalStateException(
                        "Insufficient stock for product: "
                                + product.getName()
                                + ". Available: "
                                + product.getStockQuantity()
                                + ", requested: "
                                + requestedQuantity
                );
            }

            BigDecimal subtotal =
                    product.getPrice().multiply(BigDecimal.valueOf(
                            requestedQuantity
                                    )
                            );

            totalAmount = totalAmount.add(subtotal);
        }

        /*
         * Create the parent order first.
         */
        Order order = Order.builder()
                .customer(customer)
                .address(address)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        /*
         * Create OrderItems and reduce inventory.
         *
         * priceAtPurchase is a snapshot of the current
         * product price. Future Product.price changes
         * must not alter this historical order.
         */
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            int quantity = cartItem.getQuantity();

            BigDecimal priceAtPurchase = product.getPrice();

            BigDecimal subtotal =
                    priceAtPurchase.multiply(
                            BigDecimal.valueOf(quantity)
                    );

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(quantity)
                    .priceAtPurchase(priceAtPurchase)
                    .subtotal(subtotal)
                    .build();

            OrderItem savedOrderItem =
                    orderItemRepository.save(orderItem);

            orderItems.add(savedOrderItem);

            /*
             * IMPORTANT:
             * OrderService does not directly change product stock.
             * InventoryService remains the single source of truth.
             *
             * performedByUserId = null because this stock
             * mutation is triggered by a customer purchase.
             */
            inventoryService.removeStock(
                    product.getId(),
                    quantity,
                    "Customer order #" + savedOrder.getId(),
                    null
            );
        }

        /*
         * Stock was successfully reduced for every item.
         * Now remove all items from the customer's cart.
         */
        cartItemRepository.deleteByCartId(cart.getId());

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {

        validateId(orderId, "Order ID");

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found with id: "
                                        + orderId
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomer(Long customerId) {

        validateId(customerId, "Customer ID");

        if (!userRepository.existsById(customerId)) {
            throw new IllegalArgumentException(
                    "Customer not found with id: "
                            + customerId
            );
        }

        return orderRepository
                .findByCustomerIdOrderByOrderDateDesc(
                        customerId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerAndStatus(
            Long customerId,
            OrderStatus status
    ) {

        validateId(customerId, "Customer ID");

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        if (!userRepository.existsById(customerId)) {
            throw new IllegalArgumentException(
                    "Customer not found with id: "
                            + customerId
            );
        }

        return orderRepository.findByCustomerIdAndOrderStatusOrderByOrderDateDesc(
                customerId,
                status
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(
            OrderStatus status
    ) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        return orderRepository.findByOrderStatus(status);
    }


    @Override
    public Order updateOrderStatus(
            Long orderId,
            OrderStatus newStatus,
            String cancellationReason
    ) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order id");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found with id: " + orderId
                        )
                );

        OrderStatus currentStatus = order.getOrderStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    "Invalid order status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {

            restoreOrderStock(orderId);

            order.setOrderStatus(OrderStatus.CANCELLED);

            order.setCancellationReason(
                    normalizeCancellationReason(cancellationReason)
            );
            Order savedOrder =
                    orderRepository.save(order);

            auditLogService.logCurrentAdminAction(
                    "CANCEL_ORDER",
                    "Order",
                    savedOrder.getId()
            );

            return savedOrder;
        }

        order.setOrderStatus(newStatus);

        Order savedOrder =
                orderRepository.save(order);

        auditLogService.logCurrentAdminAction(
                "UPDATE_ORDER_STATUS",
                "Order",
                savedOrder.getId()
        );

        return savedOrder;
    }

    @Override
    public Order cancelOrder(
            Long orderId,
            Long customerId,
            String cancellationReason
    ) {

        validateId(orderId, "Order ID");
        validateId(customerId, "Customer ID");

        Order order = getOrderById(orderId);

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "Order does not belong to this customer"
            );
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Order is already cancelled"
            );
        }

        if (order.getOrderStatus() == OrderStatus.PREPARING ||
                order.getOrderStatus() == OrderStatus.OUT_FOR_DELIVERY ||
                order.getOrderStatus() == OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Order cannot be cancelled after preparation has started"
            );
        }

            List<OrderItem> orderItems =
                    orderItemRepository.findByOrderId(orderId);

            for (OrderItem item : orderItems) {
                inventoryService.restoreStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        "Order #" + orderId + " cancelled - stock restored"

                );
            }



        order.setOrderStatus(OrderStatus.CANCELLED);

        order.setCancellationReason(
                normalizeCancellationReason(cancellationReason)
        );

        Order savedOrder = orderRepository.save(order);

        return savedOrder;
        }

        //admin

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getAllAdminOrders(
            OrderStatus status
    ) {

        List<Order> orders;

        if (status == null) {
            orders = orderRepository
                    .findAllByOrderByOrderDateDesc();
        } else {
            orders = orderRepository
                    .findByOrderStatusOrderByOrderDateDesc(
                            status
                    );
        }

        return orders.stream()
                .map(this::mapToAdminOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponse getAdminOrderById(
            Long orderId
    ) {

        validateId(orderId, "Order ID");

        Order order = getOrderById(orderId);

        return mapToAdminOrderResponse(order);
    }

    private AdminOrderResponse mapToAdminOrderResponse(
            Order order
    ) {
        String addressSummary = null;
        if (order.getAddress() != null) {
            Address address = order.getAddress();
            addressSummary = String.join(", ",
                    java.util.stream.Stream.of(
                            address.getAddressLine(),
                            address.getCity()
                    ).filter(s -> s != null && !s.isBlank())
                            .toList()
            );
        }

        String itemsSummary = orderItemRepository
                .findByOrderId(order.getId())
                .stream()
                .map(item -> item.getProduct() != null
                        ? item.getProduct().getName() + " x" + item.getQuantity()
                        : "")
                .filter(s -> !s.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);

        return AdminOrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .customerEmail(order.getCustomer().getEmail())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .cancellationReason(order.getCancellationReason())
                .orderDate(order.getOrderDate())
                .address(addressSummary)
                .items(itemsSummary)
                .build();
    }

    private void restoreOrderStock(Long orderId) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {

            if (item.getProduct() == null ||
                    item.getProduct().getId() == null) {

                throw new IllegalStateException(
                        "Order item product could not be identified"
                );
            }

            inventoryService.restoreStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "Order #" + orderId +
                            " cancelled - stock restored"
            );
        }
    }

    private String normalizeCancellationReason(
            String reason
    ) {

        if (reason == null ||
                reason.isBlank()) {

            return null;
        }

        String normalized =
                reason.trim();

        if (normalized.length() > 255) {

            throw new IllegalArgumentException(
                    "Cancellation reason cannot exceed 255 characters"
            );
        }

        return normalized;
    }
    }
