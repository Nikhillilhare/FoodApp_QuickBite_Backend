package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.Invoice;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.repository.InvoiceRepository;
import com.aurainfo.foodapp.repository.OrderItemRepository;
import com.aurainfo.foodapp.repository.OrderRepository;
import com.aurainfo.foodapp.service.InvoiceService;
import com.aurainfo.foodapp.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Invoice createInvoice(
            Long orderId,
            BigDecimal tax,
            BigDecimal discount
    ) {
        validateId(orderId, "Order ID");

        if (invoiceRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException(
                    "Invoice already exists for order id: " + orderId
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found with id: " + orderId
                ));
        if (order.getPaymentStatus() != PaymentStatus.PAID) {

            throw new IllegalStateException(
                    "Invoice can only be created for a paid order");
        }

        BigDecimal safeTax = nonNegativeAmount(tax, "Tax");
        BigDecimal safeDiscount = nonNegativeAmount(discount, "Discount");

        BigDecimal subtotal = orderItemRepository.findByOrderId(orderId)
                .stream()
                .map(item -> item.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (safeDiscount.compareTo(subtotal.add(safeTax)) > 0) {
            throw new IllegalArgumentException(
                    "Discount cannot exceed subtotal plus tax"
            );
        }

        BigDecimal totalAmount = subtotal
                .add(safeTax)
                .subtract(safeDiscount);

        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNumber(generateInvoiceNumber())
                .subtotal(subtotal)
                .tax(safeTax)
                .discount(safeDiscount)
                .totalAmount(totalAmount)
                .build();

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long invoiceId) {
        validateId(invoiceId, "Invoice ID");

        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invoice not found with id: " + invoiceId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(Long orderId) {
        validateId(orderId, "Order ID");

        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invoice not found for order id: " + orderId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Invoice number is required"
            );
        }

        return invoiceRepository.findByInvoiceNumber(
                        invoiceNumber.trim()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invoice not found with number: " + invoiceNumber
                ));
    }

    private BigDecimal nonNegativeAmount(
            BigDecimal amount,
            String fieldName
    ) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }

        return amount;
    }

    private String generateInvoiceNumber() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "INV-" + System.currentTimeMillis()
                    + "-" + ThreadLocalRandom.current().nextInt(100, 1000);

            if (!invoiceRepository.existsByInvoiceNumber(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique invoice number"
        );
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }
}
