package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.dto.response.BillingReportResponse;
import com.aurainfo.foodapp.dto.response.CityBreakdownResponse;
import com.aurainfo.foodapp.dto.response.DailySalesPointResponse;
import com.aurainfo.foodapp.dto.response.PaymentMethodBreakdownResponse;
import com.aurainfo.foodapp.dto.response.ReportResponse;
import com.aurainfo.foodapp.dto.response.SalesReportResponse;
import com.aurainfo.foodapp.dto.response.StockReportResponse;
import com.aurainfo.foodapp.dto.response.TopItemResponse;
import com.aurainfo.foodapp.entity.InventoryTransaction;
import com.aurainfo.foodapp.entity.InventoryTransactionType;
import com.aurainfo.foodapp.entity.Order;
import com.aurainfo.foodapp.entity.OrderStatus;
import com.aurainfo.foodapp.entity.PaymentStatus;
import com.aurainfo.foodapp.entity.ReportRange;
import com.aurainfo.foodapp.repository.InventoryTransactionRepository;
import com.aurainfo.foodapp.repository.InvoiceRepository;
import com.aurainfo.foodapp.repository.OrderItemRepository;
import com.aurainfo.foodapp.repository.OrderRepository;
import com.aurainfo.foodapp.repository.PaymentRepository;
import com.aurainfo.foodapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryTransactionRepository
            inventoryTransactionRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public SalesReportResponse getSalesReport(
            ReportRange range
    ) {

        validateRange(range);

        DateRange dateRange = getDateRange(range);

        long totalOrders =
                orderRepository.countByOrderDateBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        long cancelledOrders =
                orderRepository
                        .countByOrderStatusAndOrderDateBetween(
                                OrderStatus.CANCELLED,
                                dateRange.start(),
                                dateRange.end()
                        );

        BigDecimal totalSales =
                orderRepository.sumSalesBetween(
                        dateRange.start(),
                        dateRange.end(),
                        PaymentStatus.PAID,
                        OrderStatus.CANCELLED
                );

        long successfulOrders =
                orderRepository.countSuccessfulOrdersBetween(
                        dateRange.start(),
                        dateRange.end(),
                        PaymentStatus.PAID,
                        OrderStatus.CANCELLED
                );
        BigDecimal averageOrderValue =
                successfulOrders == 0
                        ? BigDecimal.ZERO
                        : totalSales.divide(
                        BigDecimal.valueOf(
                                successfulOrders
                        ),
                        2,
                       RoundingMode.HALF_UP
                );

        long refundedOrders =
                orderRepository.countByPaymentStatusAndOrderDateBetween(
                        PaymentStatus.REFUNDED,
                        dateRange.start(),
                        dateRange.end()
                );

        double refundRate =
                totalOrders == 0
                        ? 0.0
                        : BigDecimal.valueOf(refundedOrders)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(totalOrders), 1, RoundingMode.HALF_UP)
                                .doubleValue();

        return SalesReportResponse.builder()
                .range(range.name())
                .startDate(dateRange.start())
                .endDate(dateRange.end())
                .totalOrders(totalOrders)
                .cancelledOrders(cancelledOrders)
                .totalSales(totalSales)
                .averageOrderValue(averageOrderValue)
                .refundRate(refundRate)
                .build();
    }

    @Override
    public BillingReportResponse getBillingReport(
            ReportRange range
    ) {

        validateRange(range);

        DateRange dateRange = getDateRange(range);

        long invoiceCount =
                invoiceRepository.countByBillingDateBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        BigDecimal subtotal =
                invoiceRepository.sumSubtotalBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        BigDecimal tax =
                invoiceRepository.sumTaxBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        BigDecimal discount =
                invoiceRepository.sumDiscountBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        BigDecimal totalAmount =
                invoiceRepository.sumTotalAmountBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        return BillingReportResponse.builder()
                .range(range.name())
                .startDate(dateRange.start())
                .endDate(dateRange.end())
                .invoiceCount(invoiceCount)
                .subtotal(subtotal)
                .tax(tax)
                .discount(discount)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    public StockReportResponse getStockReport(
            ReportRange range
    ) {

        validateRange(range);

        DateRange dateRange = getDateRange(range);

        List<InventoryTransaction> transactions =
                inventoryTransactionRepository
                        .findByCreatedAtBetween(
                                dateRange.start(),
                                dateRange.end()
                        );

        long purchaseTransactions = 0;
        int purchasedQuantity = 0;

        long saleTransactions = 0;
        int soldQuantity = 0;

        long manualAdjustmentTransactions = 0;
        int manualAdjustmentQuantity = 0;

        long expiredRemovalTransactions = 0;
        int expiredRemovalQuantity = 0;

        int netStockChange = 0;

        for (InventoryTransaction transaction : transactions) {

            Integer changed =
                    transaction.getQuantityChanged();

            int quantityChanged =
                    changed == null ? 0 : changed;

            netStockChange += quantityChanged;

            InventoryTransactionType type =
                    transaction.getTransactionType();

            if (type == InventoryTransactionType.PURCHASE_IN) {

                purchaseTransactions++;
                purchasedQuantity +=
                        Math.abs(quantityChanged);

            } else if (
                    type == InventoryTransactionType.SALE
            ) {

                saleTransactions++;
                soldQuantity +=
                        Math.abs(quantityChanged);

            } else if (
                    type ==
                            InventoryTransactionType.MANUAL_ADJUSTMENT
            ) {

                manualAdjustmentTransactions++;
                manualAdjustmentQuantity +=
                        quantityChanged;

            } else if (
                    type ==
                            InventoryTransactionType.EXPIRED_REMOVAL
            ) {

                expiredRemovalTransactions++;
                expiredRemovalQuantity +=
                        Math.abs(quantityChanged);
            }
        }

        return StockReportResponse.builder()
                .range(range.name())
                .purchaseTransactions(
                        purchaseTransactions
                )
                .purchasedQuantity(
                        purchasedQuantity
                )
                .saleTransactions(
                        saleTransactions
                )
                .soldQuantity(
                        soldQuantity
                )
                .manualAdjustmentTransactions(
                        manualAdjustmentTransactions
                )
                .manualAdjustmentQuantity(
                        manualAdjustmentQuantity
                )
                .expiredRemovalTransactions(
                        expiredRemovalTransactions
                )
                .expiredRemovalQuantity(
                        expiredRemovalQuantity
                )
                .netStockChange(
                        netStockChange
                )
                .build();
    }

    private void validateRange(
            ReportRange range
    ) {

        if (range == null) {
            throw new IllegalArgumentException(
                    "Report range cannot be null"
            );
        }
    }

    private DateRange getDateRange(
            ReportRange range
    ) {

        LocalDate today = LocalDate.now();

        return switch (range) {

            case DAILY -> new DateRange(
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay()
            );

            case WEEKLY -> {

                LocalDate startOfWeek =
                        today.minusDays(
                                today.getDayOfWeek().getValue() - 1L
                        );

                yield new DateRange(
                        startOfWeek.atStartOfDay(),
                        startOfWeek.plusDays(7).atStartOfDay()
                );
            }

            case LAST_3_MONTHS -> {

                YearMonth currentMonth = YearMonth.from(today);

                yield new DateRange(
                        currentMonth
                                .minusMonths(2)
                                .atDay(1)
                                .atStartOfDay(),

                        currentMonth
                                .plusMonths(1)
                                .atDay(1)
                                .atStartOfDay()
                );
            }

            case MONTHLY -> {

                YearMonth currentMonth =
                        YearMonth.from(today);

                yield new DateRange(
                        currentMonth
                                .atDay(1)
                                .atStartOfDay(),

                        currentMonth
                                .plusMonths(1)
                                .atDay(1)
                                .atStartOfDay()
                );
            }

            case YEARLY -> {

                LocalDate firstDay =
                        LocalDate.of(
                                today.getYear(),
                                1,
                                1
                        );

                yield new DateRange(
                        firstDay.atStartOfDay(),
                        firstDay.plusYears(1)
                                .atStartOfDay()
                );
            }
        };
    }

    @Override
    public ReportResponse getCompleteReport(
            ReportRange range
    ) {

        validateRange(range);

        return ReportResponse.builder()
                .sales(getSalesReport(range))
                .billing(getBillingReport(range))
                .stock(getStockReport(range))
                .dailySales(getDailySales(range))
                .topItems(getTopItems(range))
                .paymentMethods(getPaymentMethodBreakdown(range))
                .ordersByCity(getCityBreakdown(range))
                .build();
    }

    private List<TopItemResponse> getTopItems(ReportRange range) {

        DateRange dateRange = getDateRange(range);
        Pageable top5 = PageRequest.of(0, 5);

        List<Object[]> rows =
                orderItemRepository.findTopItemsBetween(
                        dateRange.start(),
                        dateRange.end(),
                        top5
                );

        List<TopItemResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(
                    TopItemResponse.builder()
                            .productId((Long) row[0])
                            .productName((String) row[1])
                            .revenue((BigDecimal) row[2])
                            .quantitySold(((Number) row[3]).longValue())
                            .build()
            );
        }
        return result;
    }

    private List<PaymentMethodBreakdownResponse> getPaymentMethodBreakdown(ReportRange range) {

        DateRange dateRange = getDateRange(range);

        List<Object[]> rows =
                paymentRepository.findPaymentMethodBreakdownBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Object[] row : rows) {
            grandTotal = grandTotal.add((BigDecimal) row[2]);
        }

        List<PaymentMethodBreakdownResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal amount = (BigDecimal) row[2];
            double percentage =
                    grandTotal.compareTo(BigDecimal.ZERO) == 0
                            ? 0.0
                            : amount.multiply(BigDecimal.valueOf(100))
                                    .divide(grandTotal, 1, RoundingMode.HALF_UP)
                                    .doubleValue();

            result.add(
                    PaymentMethodBreakdownResponse.builder()
                            .paymentMethod(String.valueOf(row[0]))
                            .orderCount(((Number) row[1]).longValue())
                            .totalAmount(amount)
                            .percentage(percentage)
                            .build()
            );
        }
        return result;
    }

    private List<CityBreakdownResponse> getCityBreakdown(ReportRange range) {

        DateRange dateRange = getDateRange(range);

        List<Object[]> rows =
                orderRepository.findCityBreakdownBetween(
                        dateRange.start(),
                        dateRange.end(),
                        OrderStatus.CANCELLED
                );

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Object[] row : rows) {
            grandTotal = grandTotal.add((BigDecimal) row[2]);
        }

        List<CityBreakdownResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal amount = (BigDecimal) row[2];
            double percentage =
                    grandTotal.compareTo(BigDecimal.ZERO) == 0
                            ? 0.0
                            : amount.multiply(BigDecimal.valueOf(100))
                                    .divide(grandTotal, 1, RoundingMode.HALF_UP)
                                    .doubleValue();

            result.add(
                    CityBreakdownResponse.builder()
                            .city(String.valueOf(row[0]))
                            .orderCount(((Number) row[1]).longValue())
                            .totalAmount(amount)
                            .percentage(percentage)
                            .build()
            );
        }
        return result;
    }

    private static final Map<Integer, String> DAY_NAMES = Map.of(
            1, "Sun", 2, "Mon", 3, "Tue", 4, "Wed", 5, "Thu", 6, "Fri", 7, "Sat"
    );

    private List<DailySalesPointResponse> getDailySales(ReportRange range) {

        DateRange dateRange = getDateRange(range);

        List<Object[]> rows =
                orderRepository.findDailySalesBetween(
                        dateRange.start(),
                        dateRange.end()
                );

        Map<Integer, Object[]> byDow = new java.util.HashMap<>();
        for (Object[] row : rows) {
            byDow.put(((Number) row[0]).intValue(), row);
        }

        List<DailySalesPointResponse> result = new ArrayList<>();
        // Mon..Sun order to match typical weekly chart layout
        int[] mondayFirst = {2, 3, 4, 5, 6, 7, 1};
        for (int dow : mondayFirst) {
            Object[] row = byDow.get(dow);
            BigDecimal revenue = row == null ? BigDecimal.ZERO : (BigDecimal) row[1];
            long orders = row == null ? 0 : ((Number) row[2]).longValue();
            result.add(
                    DailySalesPointResponse.builder()
                            .day(DAY_NAMES.get(dow))
                            .revenue(revenue)
                            .orders(orders)
                            .build()
            );
        }
        return result;
    }

    private record DateRange(
            LocalDateTime start,
            LocalDateTime end
    ) {
    }
}