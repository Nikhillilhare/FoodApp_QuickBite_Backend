package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderItem;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.repository.OrderItemRepository;
import com.aurainfo.foodapp.repository.OrderRepository;
import com.aurainfo.foodapp.repository.ProductRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.InventoryService;
import com.aurainfo.foodapp.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long orderItemId) {

        validateId(orderItemId, "Order Item ID");

        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order item not found with id: "
                                        + orderItemId
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrder(Long orderId) {

        validateId(orderId, "Order ID");

        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException(
                    "Order not found with id: " + orderId
            );
        }

        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByProduct(Long productId) {

        validateId(productId, "Product ID");

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product not found with id: " + productId
            );
        }

        return orderItemRepository.findByProductId(productId);
    }

    @Override
    public void deleteOrderItem(Long orderItemId) {

        validateId(orderItemId, "Order Item ID");

        OrderItem orderItem = getOrderItemById(orderItemId);

        Order order = orderItem.getOrder();

        if (order == null || order.getId() == null) {

            throw new IllegalStateException(
                    "Order could not be identified for this item");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {

            throw new IllegalStateException(
                    "Order item can only be deleted while order is PENDING");
        }

        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(order.getId());

        if (orderItems.size() <= 1) {

            throw new IllegalStateException(
                    "The last order item cannot be deleted");
        }


        inventoryService.restoreStock(
                orderItem.getProduct().getId(),
                orderItem.getQuantity(),
                "Order #" + order.getId()
                        + " item deleted - stock restored"
        );

        orderItemRepository.delete(orderItem);
        orderItemRepository.flush();

        BigDecimal updatedTotal =
                orderItemRepository
                        .findByOrderId(order.getId())
                        .stream()
                        .map(OrderItem::getSubtotal)
                        .filter(java.util.Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        order.setTotalAmount(updatedTotal);

        orderRepository.save(order);

        auditLogService.logCurrentAdminAction(
                "DELETE_ORDER_ITEM",
                "OrderItem",
                orderItemId
        );
    }

    private void validateId(Long id, String fieldName) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }
}