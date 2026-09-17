package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.CreatePaymentRequest;
import com.aurainfo.foodapp.dto.response.PaymentResponse;
import com.aurainfo.foodapp.entity.Payment;
import com.aurainfo.foodapp.entity.PaymentTransactionStatus;
import com.aurainfo.foodapp.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    // =====================================================
    // MARK PAYMENT SUCCESS
    // =====================================================

    @PatchMapping("/{paymentId}/success")
    public ResponseEntity<PaymentResponse> markPaymentSuccess(
            @PathVariable Long paymentId,
            @Valid @RequestBody(required = false)
            CreatePaymentRequest request
    ) {

        String transactionRef =
                request != null
                        ? request.getTransactionRef()
                        : null;

        Payment payment =
                paymentService.markPaymentSuccess(
                        paymentId,
                        transactionRef
                );

        return ResponseEntity.ok(
                mapToResponse(payment)
        );
    }

    // =====================================================
    // MARK PAYMENT FAILED
    // =====================================================

    @PatchMapping("/{paymentId}/failed")
    public ResponseEntity<PaymentResponse> markPaymentFailed(
            @PathVariable Long paymentId
    ) {

        Payment payment =
                paymentService.markPaymentFailed(
                        paymentId
                );

        return ResponseEntity.ok(
                mapToResponse(payment)
        );
    }

    // =====================================================
    // REFUND PAYMENT
    // =====================================================

    @PatchMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long paymentId
    ) {

        Payment payment =
                paymentService.refundPayment(
                        paymentId
                );

        return ResponseEntity.ok(
                mapToResponse(payment)
        );
    }

    // =====================================================
    // GET PAYMENT BY ID
    // =====================================================

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId
    ) {

        Payment payment =
                paymentService.getPaymentById(
                        paymentId
                );

        return ResponseEntity.ok(
                mapToResponse(payment)
        );
    }

    // =====================================================
    // GET PAYMENTS BY STATUS
    // =====================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentTransactionStatus status
    ) {

        List<PaymentResponse> response =
                paymentService
                        .getPaymentsByStatus(status)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private PaymentResponse mapToResponse(
            Payment payment
    ) {

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(
                        payment.getOrder().getId()
                )
                .paymentMethod(
                        payment.getPaymentMethod()
                )
                .transactionRef(
                        payment.getTransactionRef()
                )
                .status(
                        payment.getStatus()
                )
                .paidAt(
                        payment.getPaidAt()
                )
                .build();
    }
}