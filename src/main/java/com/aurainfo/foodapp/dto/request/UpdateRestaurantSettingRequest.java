package com.aurainfo.foodapp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantSettingRequest {

    // =========================
    // General
    // =========================

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 150, message = "Restaurant name cannot exceed 150 characters")
    private String restaurantName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name cannot exceed 100 characters")
    private String ownerName;

    @NotBlank(message = "Restaurant email is required")
    @Email(message = "Restaurant email must be valid")
    @Size(max = 150, message = "Restaurant email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Restaurant phone is required")
    @Size(max = 20, message = "Restaurant phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 100, message = "FSSAI license cannot exceed 100 characters")
    private String fssaiLicense;

    @Size(max = 50, message = "GST number cannot exceed 50 characters")
    private String gstNumber;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    private LocalTime openingTime;

    private LocalTime closingTime;

    // =========================
    // Delivery
    // =========================

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Delivery radius cannot be negative"
    )
    private BigDecimal deliveryRadiusKm;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Minimum order amount cannot be negative"
    )
    private BigDecimal minimumOrderAmount;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Standard delivery fee cannot be negative"
    )
    private BigDecimal standardDeliveryFee;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Free delivery threshold cannot be negative"
    )
    private BigDecimal freeDeliveryAbove;

    @Min(
            value = 1,
            message = "Average delivery time must be greater than zero"
    )
    private Integer averageDeliveryTimeMinutes;

    @Min(
            value = 1,
            message = "Maximum concurrent orders must be greater than zero"
    )
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

    private java.util.List<String> deliveryPartners;
}