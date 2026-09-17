package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderItemResponse {

    private Long orderItemId;

    private Long productId;

    private String productName;

    private String imageUrl;

    private Integer quantity;

    private BigDecimal priceAtPurchase;

    private BigDecimal subtotal;
}