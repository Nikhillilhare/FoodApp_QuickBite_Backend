package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.Invoice;

import java.math.BigDecimal;

public interface InvoiceService {

    Invoice createInvoice(Long orderId, BigDecimal tax, BigDecimal discount);

    Invoice getInvoiceById(Long invoiceId);

    Invoice getInvoiceByOrderId(Long orderId);

    Invoice getInvoiceByNumber(String invoiceNumber);
}
