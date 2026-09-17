package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.dto.response.AdminOrderResponse;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    Order placeOrder(
            Long customerId,
            Long addressId
    );

    Order getOrderById(Long orderId);

    List<Order> getOrdersByCustomer(Long customerId);

    List<Order> getOrdersByCustomerAndStatus(
            Long customerId,
            OrderStatus status
    );

    List<Order> getOrdersByStatus(OrderStatus status);

    Order updateOrderStatus(
            Long orderId,
            OrderStatus newStatus,
            String cancellationReason
    );

    Order cancelOrder(
            Long orderId,
            Long customerId,
            String cancellationReason
    );

    AdminOrderResponse getAdminOrderById(Long orderId);

    List<AdminOrderResponse> getAllAdminOrders(OrderStatus status);

} 