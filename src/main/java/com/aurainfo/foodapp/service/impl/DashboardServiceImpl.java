package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.dto.response.DashboardResponse;
import com.aurainfo.foodapp.dto.response.RecentOrderResponse;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.PaymentStatus;
import com.aurainfo.foodapp.entity.UserRole;
import com.aurainfo.foodapp.repository.OrderItemRepository;
import com.aurainfo.foodapp.repository.OrderRepository;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;


    @Override
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startNextOfDay = today.plusDays(1).atStartOfDay();

        //Today sales

        BigDecimal todaySales = orderRepository.sumSalesBetween(
                startOfDay, startNextOfDay, PaymentStatus.PAID, OrderStatus.CANCELLED
        );

        //Today Orders

        long todayOrders = orderRepository.countByOrderDateBetween(startOfDay,startNextOfDay);

        //New Customer Today

        long newCustomers = userRepository.countByCreatedAtBetweenAndRole(
          startOfDay,startNextOfDay, UserRole.CUSTOMER
        );

        //Pending Orders

        long pendingOrders = orderRepository.countByOrderStatus(
                OrderStatus.PENDING
        );

        //Recent Orders

        List<RecentOrderResponse> recentOrders = orderRepository
                .findTop10ByOrderByOrderDateDesc()
                .stream()
                .map(this::mapRecentOrder)
                .toList();

        return DashboardResponse.builder().todaySales(todaySales)
                .todayOrders(todayOrders)
                .newCustomers(newCustomers).pendingOrders(pendingOrders)
                .recentOrders(recentOrders).build();
    }
    private RecentOrderResponse mapRecentOrder(Order order){
        String foodItems = orderItemRepository
                .findByOrderId(order.getId())
                .stream()
                .map(item -> item.getProduct() != null ? item.getProduct().getName() : "")
                .filter(name -> !name.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);

        return RecentOrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getCustomer().getName())
                .foodItems(foodItems)
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .build();
    }
}
