package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSettingResponse {

    private Long id;

    // =========================
    // General
    // =========================

    private String restaurantName;

    private String ownerName;

    private String email;

    private String phone;

    private String fssaiLicense;

    private String gstNumber;

    private String address;

    private LocalTime openingTime;

    private LocalTime closingTime;

    // =========================
    // Delivery
    // =========================

    private BigDecimal deliveryRadiusKm;

    private BigDecimal minimumOrderAmount;

    private BigDecimal standardDeliveryFee;

    private BigDecimal freeDeliveryAbove;

    private Integer averageDeliveryTimeMinutes;

    private Integer maxConcurrentOrders;

    // =========================
    // Payment Methods
    // =========================

    private Boolean upiEnabled;

    private Boolean cardEnabled;

    private Boolean cashOnDeliveryEnabled;

    private Boolean netBankingEnabled;

    // =========================
    // Notifications
    // =========================

    private Boolean newOrderReceived;

    private Boolean orderDelivered;

    private Boolean lowStockAlert;

    private Boolean newCustomerRegistered;

    private Boolean dailyReport;

    private Boolean weeklyReport;

    // =========================
    // Delivery Partners
    // =========================

    private List<String> deliveryPartners;

    // =========================
    // Audit timestamps
    // =========================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}