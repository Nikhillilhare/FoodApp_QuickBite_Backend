package com.aurainfo.foodapp.dto.request;

import com.aurainfo.foodapp.entity.PaymentMethod;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private PaymentMethod paymentMethod;

    @Size(
            max = 100,
            message = "Transaction reference cannot exceed 100 characters"
    )
    private String transactionRef;
}