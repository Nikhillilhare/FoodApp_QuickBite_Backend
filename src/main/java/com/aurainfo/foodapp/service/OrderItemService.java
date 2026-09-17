package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.OrderItem;

import java.util.List;

public interface OrderItemService {

    OrderItem getOrderItemById(Long orderItemId);

    List<OrderItem> getOrderItemsByOrder(Long orderId);

    List<OrderItem> getOrderItemsByProduct(Long productId);

    void deleteOrderItem(Long orderItemId);
}
