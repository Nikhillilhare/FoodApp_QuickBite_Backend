package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.Payment;
import com.aurainfo.foodapp.entity.PaymentMethod;
import com.aurainfo.foodapp.entity.PaymentStatus;
import com.aurainfo.foodapp.entity.PaymentTransactionStatus;
import com.aurainfo.foodapp.repository.OrderRepository;
import com.aurainfo.foodapp.repository.PaymentRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.InvoiceService;
import com.aurainfo.foodapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;
    private final InvoiceService invoiceService;

    // =====================================================
    // CREATE / INITIATE PAYMENT
    // =====================================================

    @Override
    public Payment createPayment(
            Long orderId,
            PaymentMethod paymentMethod,
            String transactionRef
    ) {

        validateId(orderId, "Order ID");

        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found with id: "
                                        + orderId
                        )
                );

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled order cannot accept payment"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Order payment is already marked as PAID"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Refunded order cannot accept a new payment"
            );
        }

        String normalizedRef =
                normalizeTransactionRef(transactionRef);

        if (normalizedRef != null &&
                paymentRepository.existsByTransactionRef(
                        normalizedRef
                )) {

            throw new IllegalStateException(
                    "Transaction reference is already in use"
            );
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .transactionRef(normalizedRef)
                .status(PaymentTransactionStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    // =====================================================
    // MARK PAYMENT SUCCESS
    // =====================================================

    @Override
    public Payment markPaymentSuccess(
            Long paymentId,
            String transactionRef
    ) {

        Payment payment =
                getPaymentById(paymentId);

        if (payment.getStatus() ==
                PaymentTransactionStatus.SUCCESS) {

            return payment;
        }

        if (payment.getStatus() ==
                PaymentTransactionStatus.FAILED) {

            throw new IllegalStateException(
                    "A failed payment cannot be marked successful"
            );
        }

        Order order = payment.getOrder();

        if (order == null || order.getId() == null) {
            throw new IllegalStateException(
                    "Payment order could not be identified"
            );
        }

        if (order.getOrderStatus() ==
                OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Cancelled order cannot be marked as paid"
            );
        }

        if (transactionRef != null &&
                !transactionRef.isBlank()) {

            String normalizedRef =
                    normalizeTransactionRef(transactionRef);

            paymentRepository.findByTransactionRef(
                            normalizedRef
                    )
                    .filter(existing ->
                            !existing.getId().equals(paymentId)
                    )
                    .ifPresent(existing -> {
                        throw new IllegalStateException(
                                "Transaction reference is already in use"
                        );
                    });

            payment.setTransactionRef(normalizedRef);
        }

        if (order.getPaymentStatus() ==
                PaymentStatus.PAID) {

            throw new IllegalStateException(
                    "Order is already paid through another payment transaction"
            );
        }

        payment.setStatus(
                PaymentTransactionStatus.SUCCESS
        );

        payment.setPaidAt(
                LocalDateTime.now()
        );

        order.setPaymentStatus(
                PaymentStatus.PAID
        );

        orderRepository.save(order);


        Payment savedPayment =
                paymentRepository.save(payment);

        invoiceService.createInvoice(
                order.getId(),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        auditLogService.logCurrentAdminAction(
                "PAYMENT_SUCCESS",
                "Payment",
                savedPayment.getId()
        );

        return savedPayment;
    }

    // =====================================================
    // MARK PAYMENT FAILED
    // =====================================================

    @Override
    public Payment markPaymentFailed(
            Long paymentId
    ) {

        Payment payment =
                getPaymentById(paymentId);

        if (payment.getStatus() ==
                PaymentTransactionStatus.SUCCESS) {

            throw new IllegalStateException(
                    "A successful payment cannot be marked failed"
            );
        }

        if (payment.getStatus() ==
                PaymentTransactionStatus.REFUNDED) {

            throw new IllegalStateException(
                    "A refunded payment cannot be marked failed"
            );
        }

        payment.setStatus(
                PaymentTransactionStatus.FAILED
        );

        Order order = payment.getOrder();

        if (order == null || order.getId() == null) {
            throw new IllegalStateException(
                    "Payment order could not be identified"
            );
        }

        order.setPaymentStatus(
                PaymentStatus.FAILED
        );

        orderRepository.save(order);

        Payment savedPayment =
                paymentRepository.save(payment);

        auditLogService.logCurrentAdminAction(
                "PAYMENT_FAILED",
                "Payment",
                savedPayment.getId()
        );

        return savedPayment;
    }

    // =====================================================
    // GET PAYMENT BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(
            Long paymentId
    ) {

        validateId(paymentId, "Payment ID");

        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with id: "
                                        + paymentId
                        )
                );
    }

    // =====================================================
    // GET PAYMENTS BY ORDER
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrder(
            Long orderId
    ) {

        validateId(orderId, "Order ID");

        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException(
                    "Order not found with id: "
                            + orderId
            );
        }

        return paymentRepository
                .findByOrderIdOrderByIdDesc(orderId);
    }

    // =====================================================
    // GET PAYMENTS BY STATUS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(
            PaymentTransactionStatus status
    ) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Payment status is required"
            );
        }

        return paymentRepository.findByStatus(status);
    }

    // =====================================================
    // REFUND PAYMENT
    // =====================================================

    @Override
    public Payment refundPayment(
            Long paymentId
    ) {

        Payment payment =
                getPaymentById(paymentId);

        if (payment.getStatus() ==
                PaymentTransactionStatus.REFUNDED) {

            throw new IllegalStateException(
                    "Payment is already refunded"
            );
        }

        if (payment.getStatus() !=
                PaymentTransactionStatus.SUCCESS) {

            throw new IllegalStateException(
                    "Only a successful payment can be refunded"
            );
        }

        Order order = payment.getOrder();

        if (order == null || order.getId() == null) {
            throw new IllegalStateException(
                    "Payment order could not be identified"
            );
        }

        payment.setStatus(
                PaymentTransactionStatus.REFUNDED
        );

        order.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        orderRepository.save(order);

        Payment savedPayment =
                paymentRepository.save(payment);

        auditLogService.logCurrentAdminAction(
                "PAYMENT_REFUND",
                "Payment",
                savedPayment.getId()
        );

        return savedPayment;
    }

    // =====================================================
    // NORMALIZE TRANSACTION REFERENCE
    // =====================================================

    private String normalizeTransactionRef(
            String transactionRef
    ) {

        if (transactionRef == null ||
                transactionRef.isBlank()) {

            return null;
        }

        String value =
                transactionRef.trim();

        if (value.length() > 100) {
            throw new IllegalArgumentException(
                    "Transaction reference cannot exceed 100 characters"
            );
        }

        return value;
    }

    // =====================================================
    // VALIDATE ID
    // =====================================================

    private void validateId(
            Long id,
            String fieldName
    ) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName +
                            " must be greater than zero"
            );
        }
    }
}