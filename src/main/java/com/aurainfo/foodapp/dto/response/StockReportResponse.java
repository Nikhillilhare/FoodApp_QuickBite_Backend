package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReportResponse {

    private String range;

    private long purchaseTransactions;

    private int purchasedQuantity;

    private long saleTransactions;

    private int soldQuantity;

    private long manualAdjustmentTransactions;

    private int manualAdjustmentQuantity;

    private long expiredRemovalTransactions;

    private int expiredRemovalQuantity;

    private int netStockChange;
}