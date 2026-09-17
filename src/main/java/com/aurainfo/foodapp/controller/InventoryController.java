package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.InventoryAdjustmentRequest;
import com.aurainfo.foodapp.dto.request.InventoryPurchaseRequest;
import com.aurainfo.foodapp.dto.request.InventoryRemovalRequest;
import com.aurainfo.foodapp.dto.response.InventoryStockResponse;
import com.aurainfo.foodapp.dto.response.InventoryTransactionResponse;
import com.aurainfo.foodapp.dto.response.ProductResponse;
import com.aurainfo.foodapp.entity.InventoryTransaction;
import com.aurainfo.foodapp.entity.Product;
import com.aurainfo.foodapp.service.InventoryService;
import com.aurainfo.foodapp.service.ProductService;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final UserService userService;

    // =====================================================
    // PURCHASE STOCK
    // =====================================================

    @PostMapping("/{productId}/purchase")
    public ResponseEntity<InventoryTransactionResponse> purchaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryPurchaseRequest request,
            Authentication authentication
    ) {

        Long adminUserId = getAuthenticatedUserId(authentication);

        InventoryTransaction transaction =
                inventoryService.purchaseStock(
                        productId,
                        request.getQuantity(),
                        request.getReason(),
                        adminUserId
                );

        return ResponseEntity.ok(
                mapToTransactionResponse(transaction)
        );
    }

    // =====================================================
    // MANUAL STOCK ADJUSTMENT
    // =====================================================

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryTransactionResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentRequest request,
            Authentication authentication
    ) {

        Long adminUserId = getAuthenticatedUserId(authentication);

        InventoryTransaction transaction =
                inventoryService.recordManualAdjustment(
                        productId,
                        request.getNewQuantity(),
                        request.getReason(),
                        adminUserId
                );

        return ResponseEntity.ok(
                mapToTransactionResponse(transaction)
        );
    }

    // =====================================================
    // EXPIRED STOCK REMOVAL
    // =====================================================

    @PostMapping("/{productId}/expired-removal")
    public ResponseEntity<InventoryTransactionResponse> removeExpiredStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryRemovalRequest request,
            Authentication authentication
    ) {

        Long adminUserId = getAuthenticatedUserId(authentication);

        InventoryTransaction transaction =
                inventoryService.recordExpiredRemoval(
                        productId,
                        request.getQuantity(),
                        request.getReason(),
                        adminUserId
                );

        return ResponseEntity.ok(
                mapToTransactionResponse(transaction)
        );
    }

    // =====================================================
    // INVENTORY HISTORY
    // =====================================================

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<InventoryTransactionResponse>> getInventoryHistory(
            @PathVariable Long productId
    ) {

        List<InventoryTransactionResponse> response =
                inventoryService
                        .getProductInventoryHistory(productId)
                        .stream()
                        .map(this::mapToTransactionResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // CURRENT STOCK
    // =====================================================

    @GetMapping("/{productId}/current-stock")
    public ResponseEntity<InventoryStockResponse> getCurrentStock(
            @PathVariable Long productId
    ) {

        Product product =
                productService.getProductById(productId);

        int currentStock = inventoryService.getCurrentStock(productId);

        boolean lowStock = inventoryService.isLowStock(productId);

        return ResponseEntity.ok(
                InventoryStockResponse.builder()
                        .productId(productId)
                        .productName(product.getName())
                        .currentStock(currentStock)
                        .minimumStockThreshold(product.getMinStockThreshold())
                        .lowStock(lowStock)
                        .build()
        );
    }

    // =====================================================
    // LOW STOCK CHECK
    // =====================================================

    @GetMapping("/{productId}/low-stock-check")
    public ResponseEntity<InventoryStockResponse> checkLowStock(
            @PathVariable Long productId
    ) {

        Product product =
                productService.getProductById(productId);
        int currentStock =
                inventoryService.getCurrentStock(productId);

        boolean lowStock =
                inventoryService.isLowStock(productId);

        return ResponseEntity.ok(
                InventoryStockResponse.builder()
                        .productId(productId)
                        .productName(product.getName())
                        .currentStock(currentStock)
                        .minimumStockThreshold(product.getMinStockThreshold())
                        .lowStock(lowStock)
                        .build()
        );
    }

    // =====================================================
    // ALL LOW-STOCK PRODUCTS
    // =====================================================

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {

        List<ProductResponse> response =
                inventoryService.getLowStockProducts()
                        .stream()
                        .map(this::mapToProductResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // AUTHENTICATED ADMIN
    // =====================================================

    private Long getAuthenticatedUserId(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated user could not be identified"
            );
        }

        return userService
                .getUserByEmail(authentication.getName())
                .getId();
    }

    // =====================================================
    // INVENTORY TRANSACTION MAPPER
    // =====================================================

    private InventoryTransactionResponse mapToTransactionResponse(
            InventoryTransaction transaction
    ) {

        return InventoryTransactionResponse.builder()
                .id(transaction.getId())
                .productId(
                        transaction.getProduct().getId()
                )
                .productName(
                        transaction.getProduct().getName()
                )
                .transactionType(
                        transaction.getTransactionType()
                )
                .quantityChanged(
                        transaction.getQuantityChanged()
                )
                .previousQuantity(
                        transaction.getPreviousQuantity()
                )
                .newQuantity(
                        transaction.getNewQuantity()
                )
                .reason(
                        transaction.getReason()
                )
                .performedByUserId(
                        transaction.getPerformedBy() != null
                                ? transaction.getPerformedBy().getId()
                                : null
                )
                .performedByUserName(
                        transaction.getPerformedBy() != null
                                ? transaction.getPerformedBy().getName()
                                : null
                )
                .createdAt(
                        transaction.getCreatedAt()
                )
                .build();
    }

    // =====================================================
    // PRODUCT RESPONSE MAPPER
    // =====================================================

    private ProductResponse mapToProductResponse(
            Product product
    ) {

        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(
                        product.getCategory().getId()
                )
                .categoryName(
                        product.getCategory().getName()
                )
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .manufacturingDate(
                        product.getManufacturingDate()
                )
                .expiryDate(
                        product.getExpiryDate()
                )
                .stockQuantity(
                        product.getStockQuantity()
                )
                .minStockThreshold(
                        product.getMinStockThreshold()
                )
                .status(
                        product.getStatus()
                )
                .createdAt(
                        product.getCreatedAt()
                )
                .updatedAt(
                        product.getUpdatedAt()
                )
                .build();
    }


}