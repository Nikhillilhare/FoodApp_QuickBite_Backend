package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.InventoryTransaction;
import com.aurainfo.foodapp.entity.Product;

import java.util.List;

public interface InventoryService {

    InventoryTransaction purchaseStock(
            Long productId,
            int quantity,
            String reason,
            Long performedByUserId
    );

    InventoryTransaction removeStock(
            Long productId,
            int quantity,
            String reason,
            Long performedByUserId
    );

    InventoryTransaction recordManualAdjustment(
            Long productId,
            int newQuantity,
            String reason,
            Long performedByUserId
    );

    InventoryTransaction recordExpiredRemoval(
            Long productId,
            int quantity,
            String reason,
            Long performedByUserId
    );

    List<InventoryTransaction> getProductInventoryHistory(
            Long productId
    );

    int getCurrentStock(Long productId);

    boolean isLowStock(Long productId);

    InventoryTransaction restoreStock(
            Long productId,
            int quantity,
            String reason
    );

    List<Product> getLowStockProducts();
}