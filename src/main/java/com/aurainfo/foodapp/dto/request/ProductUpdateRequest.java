package com.aurainfo.foodapp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 150, message = "Product name cannot exceed 150 characters")
    private String name;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Product price cannot be negative"
    )
    private BigDecimal price;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    private Integer minStockThreshold;
}