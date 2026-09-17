package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodBreakdownResponse {

    private String paymentMethod;

    private long orderCount;

    private BigDecimal totalAmount;

    private double percentage;
}
