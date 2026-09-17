package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockResponse {

    private Long productId;
    private String productName;

    private Integer currentStock;

    private Integer minimumStockThreshold;
    private boolean lowStock;
}