package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.ProductCreateRequest;
import com.aurainfo.foodapp.dto.request.ProductUpdateRequest;
import com.aurainfo.foodapp.dto.response.ProductResponse;
import com.aurainfo.foodapp.entity.Category;
import com.aurainfo.foodapp.entity.Product;
import com.aurainfo.foodapp.entity.ProductStatus;
import com.aurainfo.foodapp.service.CategoryService;
import com.aurainfo.foodapp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // =====================================================
    // PUBLIC PRODUCT APIs
    // =====================================================

    @GetMapping("/api/products")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {

        List<ProductResponse> response =
                productService.getActiveProduct()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/products/{productId}")
    public ResponseEntity<ProductResponse> getPublicProduct(
            @PathVariable Long productId
    ) {
        Product product = productService.getProductById(productId);

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Product not found with id: " + productId
            );
        }

        return ResponseEntity.ok(mapToResponse(product));
    }

    // =====================================================
    // ADMIN PRODUCT APIs
    // =====================================================

    @PostMapping("/api/admin/products")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {

        Category category =
                categoryService.getCategoryById(
                        request.getCategoryId()
                );

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .manufacturingDate(
                        request.getManufacturingDate()
                )
                .expiryDate(
                        request.getExpiryDate()
                )
                .minStockThreshold(
                        request.getMinStockThreshold()
                )
                .build();

        Product savedProduct =
                productService.createProduct(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(savedProduct));
    }

    @GetMapping("/api/admin/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> response =
                productService.getAllProduct()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/products/{productId}")
    public ResponseEntity<ProductResponse> getAdminProduct(
            @PathVariable Long productId
    ) {

        Product product =
                productService.getProductById(productId);

        return ResponseEntity.ok(
                mapToResponse(product)
        );
    }

    @PutMapping("/api/admin/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {

        Product updateProduct = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .manufacturingDate(
                        request.getManufacturingDate()
                )
                .expiryDate(
                        request.getExpiryDate()
                )
                .minStockThreshold(
                        request.getMinStockThreshold()
                )
                .build();

        Product updatedProduct =
                productService.updateProduct(
                        productId,
                        updateProduct
                );

        return ResponseEntity.ok(
                mapToResponse(updatedProduct)
        );
    }

    @PatchMapping("/api/admin/products/{productId}/deactivate")
    public ResponseEntity<String> deactivateProduct(
            @PathVariable Long productId
    ) {

        productService.deactiveProduct(productId);

        return ResponseEntity.ok(
                "Product deactivated successfully"
        );
    }

    @PatchMapping("/api/admin/products/{productId}/activate")
    public ResponseEntity<String> activateProduct(
            @PathVariable Long productId
    ) {

        productService.activateProduct(productId);

        return ResponseEntity.ok(
                "Product activated successfully"
        );
    }

    @GetMapping("/api/admin/products/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String name
    ) {

        List<ProductResponse> response =
                productService.searchProduct(name)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/products/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId
    ) {

        List<ProductResponse> response =
                productService
                        .getProductByCategory(categoryId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // MAPPER
    // =====================================================

    private ProductResponse mapToResponse(
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
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}