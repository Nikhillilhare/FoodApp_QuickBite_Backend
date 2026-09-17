package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.Payment;
import com.aurainfo.foodapp.entity.PaymentMethod;
import com.aurainfo.foodapp.entity.PaymentTransactionStatus;

import java.util.List;

public interface PaymentService {

    Payment createPayment(
            Long orderId, PaymentMethod paymentMethod, String transactionRef);

    Payment markPaymentSuccess(Long paymentId, String transactionRef);

    Payment markPaymentFailed(Long paymentId);

    Payment getPaymentById(Long paymentId);

    List<Payment> getPaymentsByOrder(Long orderId);

    List<Payment> getPaymentsByStatus(PaymentTransactionStatus status);

    Payment refundPayment(Long paymentId);
}
