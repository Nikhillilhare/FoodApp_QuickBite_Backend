package com.aurainfo.foodapp.dto.response;

import com.aurainfo.foodapp.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    private Integer stockQuantity;

    private Integer minStockThreshold;

    private ProductStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}