package com.aurainfo.foodapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentRequest {

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "New quantity cannot be negative")
    private Integer newQuantity;

    @Size(
            max = 255,
            message = "Reason cannot exceed 255 characters"
    )
    private String reason;
}