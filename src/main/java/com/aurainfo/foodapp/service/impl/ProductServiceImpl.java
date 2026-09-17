package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Category;
import com.aurainfo.foodapp.entity.Product;
import com.aurainfo.foodapp.entity.ProductStatus;
import com.aurainfo.foodapp.repository.CategoryRepository;
import com.aurainfo.foodapp.repository.ProductRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    @Override
    public Product createProduct(Product product) {
        validateProduct(product);
        Long categoryId = product.getCategory().getId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()->
                new IllegalArgumentException("Category Not Found")
        );
        validateCategory(category);
        product.setCategory(category);

        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        if (product.getMinStockThreshold() == null) {
            product.setMinStockThreshold(10);
        }


        /*
         * Reload with Category fetched.
         * This makes the returned Product safe for
         * ProductController -> ProductResponse mapping.
         */
        Product savedProduct = productRepository.save(product);

        Product result =
                productRepository
                        .findByIdWithCategory(savedProduct.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Product could not be reloaded after creation"
                                )
                        );

        auditLogService.logCurrentAdminAction(
                "CREATE_PRODUCT",
                "Product",
                result.getId()
        );

        return result;

    }
    // =====================================================
    // GET PRODUCT BY ID
    // =====================================================
    //
    // IMPORTANT:
    // No ACTIVE check here.
    //
    // Customer controller performs ACTIVE validation.
    // Admin controller can see ACTIVE and INACTIVE.
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        validateId(productId, "Product ID");
        return productRepository
                .findByIdWithCategory(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found with id: "
                                        + productId
                        )
                );
    }

    // =====================================================
    // ADMIN - GET ALL PRODUCTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProduct() {
        return productRepository.findAllWithCategory();
    }

    // =====================================================
    // CUSTOMER - GET ACTIVE PRODUCTS
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<Product> getActiveProduct() {
        return productRepository.findByStatusWithCategory(ProductStatus.ACTIVE);
    }


    // =====================================================
    // ACTIVE PRODUCTS BY CATEGORY
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductByCategory(Long categoryId) {

        validateId(categoryId, "Category ID");

        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException(
                    "Category not found with id: " + categoryId);
        }
        return productRepository
                .findByCategoryIdAndStatusWithCategory(
                        categoryId, ProductStatus.ACTIVE);
    }

    // =====================================================
    // ADMIN - SEARCH
    // =====================================================
    @Override
    public List<Product> searchProduct(String name) {
        if (name==null || name.isBlank()){
            throw new IllegalArgumentException("Product Search Name Cannot be Empty");
        }
        return productRepository.searchByNameWithCategory(name.trim());
    }

    // =====================================================
    // ADMIN - UPDATE PRODUCT
    // =====================================================

    @Override
    public Product updateProduct(Long productId, Product updateProduct) {

        validateId(productId, "Product ID");
        if (updateProduct == null) {
            throw new IllegalArgumentException(
                    "Product update data cannot be null");
        }

        Product existingProduct = getProductById(productId);
        if (updateProduct.getName()!=null &&
                !updateProduct.getName().isBlank()){
            existingProduct.setName(updateProduct.getName().trim());
        }
        if (updateProduct.getDescription()!=null){
            existingProduct.setDescription(updateProduct.getDescription());
        }

        if (updateProduct.getPrice() != null) {
            validatePrice(updateProduct.getPrice());

            existingProduct.setPrice(
                    updateProduct.getPrice()
            );
        }

        if (updateProduct.getImageUrl() != null) {
            existingProduct.setImageUrl(
                    updateProduct.getImageUrl()
            );
        }


        if (updateProduct.getManufacturingDate() != null) {
            existingProduct.setManufacturingDate(
                    updateProduct.getManufacturingDate()
            );
        }

        if (updateProduct.getExpiryDate() != null) {
            existingProduct.setExpiryDate(
                    updateProduct.getExpiryDate()
            );
        }

        // -------------------------------------------------
        // DATE VALIDATION
        // -------------------------------------------------

        validateDates(existingProduct.getManufacturingDate(),
                existingProduct.getExpiryDate());

        if (updateProduct.getMinStockThreshold() != null) {
            if (updateProduct.getMinStockThreshold() < 0) {
                throw new IllegalArgumentException(
                        "Minimum stock threshold cannot be negative");
            }

            existingProduct.setMinStockThreshold(
                    updateProduct.getMinStockThreshold());
        }

        if (updateProduct.getStatus() != null) {
            existingProduct.setStatus(updateProduct.getStatus());
        }

        Product savedProduct =
                productRepository.save(existingProduct);

        Product result =
                productRepository.findByIdWithCategory(savedProduct.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Product could not be reloaded after update"
                                )
                        );

        auditLogService.logCurrentAdminAction(
                "UPDATE_PRODUCT",
                "Product",
                result.getId()
        );

        return result;
    }

    @Override
    public void deactiveProduct(Long productId) {
        Product product = getProductById(productId);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
        auditLogService.logCurrentAdminAction(
                "DEACTIVATE_PRODUCT",
                "Product",
                product.getId()
        );
    }

    @Override
    public void activateProduct(Long productId) {
        Product product = getProductById(productId);
        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
        auditLogService.logCurrentAdminAction(
                "ACTIVATE_PRODUCT",
                "Product",
                product.getId()
        );
    }

    // REMOVED: increaseStock(...) and decreaseStock(...)
    // See the comment in ProductService.java for why — stock mutation now
    // only happens through InventoryService, which also logs an
    // InventoryTransaction row every time. These two methods used to update
    // stockQuantity directly with no logging at all (the `reason` parameter
    // they took was accepted but never actually used anywhere).

    private void validateProduct(Product product){
        if (product == null){
            throw new IllegalArgumentException("Product Cannot be null");
        }
        if (product.getName()==null || product.getName().isBlank()){
            throw new IllegalArgumentException("Product Name is required");
        }
        validatePrice(product.getPrice());
        if (product.getCategory()==null || product.getCategory().getId() == null){
            throw new IllegalArgumentException("Product Category Required");
        }
        if(product.getStockQuantity() !=null && product.getStockQuantity()<0){
            // FIX (Bug 3): this message used to say "Minimum Stock Threshold
            // cannot be Negative", which was wrong — the check above is on
            // getStockQuantity(), not getMinStockThreshold(). Corrected below.
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (product.getMinStockThreshold() != null &&
                product.getMinStockThreshold() < 0) {

            throw new IllegalArgumentException(
                    "Minimum stock threshold cannot be negative"
            );
        }
        validateDates(product.getManufacturingDate(),product.getExpiryDate());
    }
    private void validatePrice(BigDecimal price){
        if(price == null || price.signum() < 0){
            throw new IllegalArgumentException("Product Price Cannot be Negative");
        }
    }
    private void validateDates(LocalDate manufacturingDate, LocalDate expiryDate){
        if(manufacturingDate != null && expiryDate != null &&
                expiryDate.isBefore(manufacturingDate)){
            throw new IllegalArgumentException(
                    "Expiry date cannot be before manufacturing date"
            );
        }
    }
    private void validateCategory(Category category){
        if (category.getStatus() == null || category.getStatus().name().equals("INACTIVE")){
            throw new IllegalArgumentException(
                    "Cannot create product under an inactive category"
            );
        }
    }


    private void validateId(Long id, String fieldName) {

        if (id == null || id <= 0) {

            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }
}
