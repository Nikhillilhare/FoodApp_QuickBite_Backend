package com.aurainfo.foodapp.dto.response;

import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentOrderResponse {

    private Long orderId;

    private String customerName;

    // Comma separated product names for this order, e.g. "Butter Chicken, Naan"
    private String foodItems;

    private BigDecimal totalAmount;

    private PaymentStatus paymentStatus;

    private OrderStatus orderStatus;

    private LocalDateTime orderDate;
}