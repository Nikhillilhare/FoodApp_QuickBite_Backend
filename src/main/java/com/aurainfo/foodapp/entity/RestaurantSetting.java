package com.aurainfo.foodapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "restaurant_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // General
    // =========================

    @Column(name = "restaurant_name", nullable = false, length = 150)
    private String restaurantName;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "fssai_license", length = 100)
    private String fssaiLicense;

    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    @Column(length = 500)
    private String address;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    // =========================
    // Delivery
    // =========================

    @Column(name = "delivery_radius_km",
            nullable = false,
            precision = 5,
            scale = 2)
    @Builder.Default
    private BigDecimal deliveryRadiusKm = BigDecimal.TEN;

    @Column(name = "minimum_order_amount",
            nullable = false,
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "standard_delivery_fee",
            nullable = false,
            precision = 10,
            scale = 2)
    @Builder.Default
    private BigDecimal standardDeliveryFee = BigDecimal.ZERO;

    @Column(name = "free_delivery_above",
            precision = 10,
            scale = 2)
    private BigDecimal freeDeliveryAbove;

    @Column(name = "average_delivery_time_minutes",
            nullable = false)
    @Builder.Default
    private Integer averageDeliveryTimeMinutes = 30;

    @Column(name = "max_concurrent_orders",
            nullable = false)
    @Builder.Default
    private Integer maxConcurrentOrders = 10;

    // =========================
    // Payment Methods
    // =========================

    @Column(name = "upi_enabled", nullable = false)
    @Builder.Default
    private Boolean upiEnabled = true;

    @Column(name = "card_enabled", nullable = false)
    @Builder.Default
    private Boolean cardEnabled = true;

    @Column(name = "cash_on_delivery_enabled", nullable = false)
    @Builder.Default
    private Boolean cashOnDeliveryEnabled = true;

    @Column(name = "netbanking_enabled", nullable = false)
    @Builder.Default
    private Boolean netBankingEnabled = true;

    // =========================
    // Notifications
    // =========================

    @Column(name = "new_order_received", nullable = false)
    @Builder.Default
    private Boolean newOrderReceived = true;

    @Column(name = "order_delivered", nullable = false)
    @Builder.Default
    private Boolean orderDelivered = true;

    @Column(name = "low_stock_alert", nullable = false)
    @Builder.Default
    private Boolean lowStockAlert = true;

    @Column(name = "new_customer_registered", nullable = false)
    @Builder.Default
    private Boolean newCustomerRegistered = true;

    @Column(name = "daily_report", nullable = false)
    @Builder.Default
    private Boolean dailyReport = true;

    @Column(name = "weekly_report", nullable = false)
    @Builder.Default
    private Boolean weeklyReport = false;

    // =========================
    // Delivery Partners (comma-separated list, e.g. "Swiggy,Zomato")
    // =========================

    @Column(name = "delivery_partners", length = 255)
    private String deliveryPartners;

    // =========================
    // Audit timestamps
    // =========================

    @CreationTimestamp
    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",
            nullable = false)
    private LocalDateTime updatedAt;
}