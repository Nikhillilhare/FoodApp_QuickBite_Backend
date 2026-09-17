package com.aurainfo.foodapp.dto.response;

import com.aurainfo.foodapp.entity.InventoryTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionResponse {

    private Long id;

    private Long productId;

    private String productName;

    private InventoryTransactionType transactionType;

    private Integer quantityChanged;

    private Integer previousQuantity;

    private Integer newQuantity;

    private String reason;

    private Long performedByUserId;

    private String performedByUserName;

    private LocalDateTime createdAt;
}