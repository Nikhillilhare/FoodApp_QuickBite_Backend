package com.aurainfo.foodapp.dto.response;

import com.aurainfo.foodapp.entity.PaymentMethod;
import com.aurainfo.foodapp.entity.PaymentTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;

    private Long orderId;

    private PaymentMethod paymentMethod;

    private String transactionRef;

    private PaymentTransactionStatus status;

    private LocalDateTime paidAt;
}