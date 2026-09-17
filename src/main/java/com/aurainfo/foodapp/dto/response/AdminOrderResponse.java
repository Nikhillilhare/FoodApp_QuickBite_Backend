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
public class AdminOrderResponse {

    private Long orderId;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private BigDecimal totalAmount;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private String cancellationReason;

    private LocalDateTime orderDate;

    // Delivery address summary, e.g. "12 MG Road, Bengaluru"
    private String address;

    // Comma separated product names for this order, e.g. "Butter Chicken, Naan"
    private String items;
}