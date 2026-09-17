package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.*;
import com.aurainfo.foodapp.repository.InventoryTransactionRepository;
import com.aurainfo.foodapp.repository.ProductRepository;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public InventoryTransaction purchaseStock(Long productId, int quantity, String reason, Long performedByUserId) {
        validatePositiveQuantity(quantity);
        Product product = getProduct(productId);
        validateProductCanBeUpdated(product);
        int previousQuantity = product.getStockQuantity();
        int newQuantity = Math.addExact(previousQuantity, quantity);
        product.setStockQuantity(newQuantity);
        Product saveProduct = productRepository.save(product);
        User performedBy = getPerformedByUser(performedByUserId);
        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(saveProduct)
                .transactionType(InventoryTransactionType.PURCHASE_IN)
                .quantityChanged(quantity)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reason(normalizeReason(reason))
                .performedBy(performedBy)
                .build();

        InventoryTransaction savedTransaction =
                inventoryTransactionRepository.save(transaction);

        auditLogService.logCurrentAdminAction(
                "PURCHASE_STOCK",
                "Product",
                productId
        );

        return savedTransaction;
    }

    @Override
    public InventoryTransaction removeStock(Long productId, int quantity, String reason, Long performedByUserId) {
        validatePositiveQuantity(quantity);
        Product product = getProduct(productId);
        validateProductCanBeUpdated(product);
        int previousQuantity = product.getStockQuantity();
        if (previousQuantity < quantity){
            throw new IllegalStateException(
                    "Insufficient stock. Current Stock"
                            +previousQuantity + ", requested: "
                            + quantity
            );
        }
        int newQuantity = previousQuantity - quantity;
        product.setStockQuantity(newQuantity);

        Product saveProduct = productRepository.save(product);

        User performedBy = getPerformedByUser(performedByUserId);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(saveProduct).transactionType(InventoryTransactionType.SALE)
                .quantityChanged(-quantity).previousQuantity(previousQuantity)
                .newQuantity(newQuantity).reason(normalizeReason(reason))
                .performedBy(performedBy).build();

        return inventoryTransactionRepository.save(transaction);
    }

    @Override
    public InventoryTransaction recordManualAdjustment(Long productId, int newQuantity, String reason, Long performedByUserId) {
        validateNonNegativeQuantity(newQuantity);

        Product product = getProduct(productId);

        validateProductCanBeUpdated(product);

        int previousQuantity = product.getStockQuantity();

        int quantityChanged = newQuantity - previousQuantity;

        // FIX (Bug 1): this must be set to newQuantity (the actual new total),
        // NOT quantityChanged (the delta). The old code set stock to the
        // difference instead of the new total, corrupting the stock count
        // on every manual adjustment.
        product.setStockQuantity(newQuantity);

        Product savedProduct = productRepository.save(product);

        User performedBy = getPerformedByUser(performedByUserId);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(savedProduct)
                .transactionType(InventoryTransactionType.MANUAL_ADJUSTMENT)
                .quantityChanged(quantityChanged).previousQuantity(previousQuantity)
                .newQuantity(newQuantity).reason(normalizeReason(reason))
                .performedBy(performedBy)
                .build();

        InventoryTransaction savedTransaction =
                inventoryTransactionRepository.save(transaction);

        auditLogService.logCurrentAdminAction(
                "ADJUST_STOCK",
                "Product",
                productId
        );

        return savedTransaction;
    }

    @Override
    public InventoryTransaction recordExpiredRemoval(Long productId, int quantity, String reason, Long performedByUserId) {
        validatePositiveQuantity(quantity);
        Product product = getProduct(productId);
        validateProductCanBeUpdated(product);
        int previousQuantity = product.getStockQuantity();
        if(previousQuantity < quantity){
            throw new IllegalStateException(
                    "Cannot remove more expired stock than current stock"
            );
        }
        int newQuantity = previousQuantity - quantity;
        product.setStockQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);
        User performedBy = getPerformedByUser(performedByUserId);
        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(savedProduct).transactionType(InventoryTransactionType.EXPIRED_REMOVAL)
                .quantityChanged(-quantity).previousQuantity(previousQuantity)
                .newQuantity(newQuantity).reason(normalizeReason(reason))
                .performedBy(performedBy).build();
        InventoryTransaction savedTransaction =
                inventoryTransactionRepository.save(transaction);

        auditLogService.logCurrentAdminAction(
                "REMOVE_EXPIRED_STOCK",
                "Product",
                productId
        );

        return savedTransaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getProductInventoryHistory(Long productId) {
        validateId(productId, "Product ID");
        if (!productRepository.existsById(productId)){
            throw new IllegalArgumentException("Product Not found With id: "+productId);
        }
        return  inventoryTransactionRepository.findHistoryByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCurrentStock(Long productId) {
        Product product = getProduct(productId);
        return  product.getStockQuantity();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLowStock(Long productId) {
        Product product = getProduct(productId);
        return product.getStockQuantity()<= product.getMinStockThreshold();
    }

    private Product getProduct(Long productId){
        if (productId == null || productId <=0){
            throw new IllegalArgumentException(
                    "Product Id Cannot be Null or Zero"
            );
        }
        return productRepository.findById(productId).orElseThrow(()->
                new IllegalArgumentException(
                        "Product not found with id: "+productId
                ));
    }

    private void validateProductCanBeUpdated(Product product){
        if (product.getStatus()!= ProductStatus.ACTIVE){
            throw new IllegalStateException(
                    "Cannot be Modify inventory for an InActive Product"
            );
        }
        if (product.getStockQuantity()<0){
            throw new IllegalStateException(
                    "Product Stock cannot be Negative"
            );
        }
    }

    private void validatePositiveQuantity(int quantity){
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    private void validateNonNegativeQuantity(int quantity){
        if(quantity < 0){
            throw new IllegalArgumentException(
                    "Quantity cannot be Negative"
            );
        }
    }

    private String normalizeReason(String reason){
        if(reason == null || reason.isBlank()){
            return null;
        }
        return reason.trim();
    }

    private User getPerformedByUser(Long userId) {

        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Performed By User not found with id: " + userId
                        )
                );
    }
    @Override
    public InventoryTransaction restoreStock(
            Long productId,
            int quantity,
            String reason
    ) {

        validatePositiveQuantity(quantity);

        Product product = getProduct(productId);

        validateProductCanBeUpdated(product);

        int previousQuantity = product.getStockQuantity();

        int newQuantity = Math.addExact(
                previousQuantity,
                quantity
        );

        product.setStockQuantity(newQuantity);

        Product savedProduct =
                productRepository.save(product);

        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .product(savedProduct)
                        .transactionType(
                                InventoryTransactionType.MANUAL_ADJUSTMENT
                        )
                        .quantityChanged(quantity)
                        .previousQuantity(previousQuantity)
                        .newQuantity(newQuantity)
                        .reason(normalizeReason(reason))
                        .performedBy(null)
                        .build();

        return inventoryTransactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {

        return productRepository
                .findLowStockProducts();
    }

    private void validateId(
            Long id,
            String fieldName
    ) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero");
        }
    }
}
