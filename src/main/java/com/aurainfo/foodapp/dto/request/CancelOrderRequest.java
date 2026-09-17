package com.aurainfo.foodapp.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

    @Size(
            max = 255,
            message = "Cancellation reason cannot exceed 255 characters"
    )
    private String cancellationReason;
}