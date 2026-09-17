package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.CreateRestaurantSettingRequest;
import com.aurainfo.foodapp.dto.request.UpdateRestaurantSettingRequest;
import com.aurainfo.foodapp.dto.response.RestaurantSettingResponse;
import com.aurainfo.foodapp.entity.RestaurantSetting;
import com.aurainfo.foodapp.service.RestaurantSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class RestaurantSettingController {

    private final RestaurantSettingService restaurantSettingService;

    // =====================================================
    // GET SETTINGS
    // =====================================================

    @GetMapping
    public ResponseEntity<RestaurantSettingResponse> getSettings() {

        RestaurantSetting setting =
                restaurantSettingService.getSettings();

        return ResponseEntity.ok(
                mapToResponse(setting)
        );
    }

    // =====================================================
    // CREATE SETTINGS
    // =====================================================

    @PostMapping
    public ResponseEntity<RestaurantSettingResponse> createSettings(
            @Valid @RequestBody CreateRestaurantSettingRequest request
    ) {

        RestaurantSetting setting =
                RestaurantSetting.builder()
                        .restaurantName(request.getRestaurantName())
                        .ownerName(request.getOwnerName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .fssaiLicense(request.getFssaiLicense())
                        .gstNumber(request.getGstNumber())
                        .address(request.getAddress())
                        .openingTime(request.getOpeningTime())
                        .closingTime(request.getClosingTime())
                        .deliveryRadiusKm(request.getDeliveryRadiusKm())
                        .minimumOrderAmount(
                                request.getMinimumOrderAmount()
                        )
                        .standardDeliveryFee(
                                request.getStandardDeliveryFee()
                        )
                        .freeDeliveryAbove(
                                request.getFreeDeliveryAbove()
                        )
                        .averageDeliveryTimeMinutes(
                                request.getAverageDeliveryTimeMinutes()
                        )
                        .maxConcurrentOrders(
                                request.getMaxConcurrentOrders()
                        )
                        .upiEnabled(request.getUpiEnabled())
                        .cardEnabled(request.getCardEnabled())
                        .cashOnDeliveryEnabled(
                                request.getCashOnDeliveryEnabled()
                        )
                        .netBankingEnabled(
                                request.getNetBankingEnabled()
                        )
                        .newOrderReceived(
                                request.getNewOrderReceived()
                        )
                        .orderDelivered(
                                request.getOrderDelivered()
                        )
                        .lowStockAlert(
                                request.getLowStockAlert()
                        )
                        .newCustomerRegistered(
                                request.getNewCustomerRegistered()
                        )
                        .dailyReport(
                                request.getDailyReport()
                        )
                        .weeklyReport(
                                request.getWeeklyReport()
                        )
                        .deliveryPartners(
                                joinPartners(request.getDeliveryPartners())
                        )
                        .build();

        RestaurantSetting savedSetting =
                restaurantSettingService.createSettings(setting);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(savedSetting));
    }

    // =====================================================
    // UPDATE SETTINGS
    // =====================================================

    @PutMapping
    public ResponseEntity<RestaurantSettingResponse> updateSettings(
            @Valid @RequestBody UpdateRestaurantSettingRequest request
    ) {

        RestaurantSetting setting =
                RestaurantSetting.builder()
                        .restaurantName(request.getRestaurantName())
                        .ownerName(request.getOwnerName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .fssaiLicense(request.getFssaiLicense())
                        .gstNumber(request.getGstNumber())
                        .address(request.getAddress())
                        .openingTime(request.getOpeningTime())
                        .closingTime(request.getClosingTime())
                        .deliveryRadiusKm(request.getDeliveryRadiusKm())
                        .minimumOrderAmount(
                                request.getMinimumOrderAmount()
                        )
                        .standardDeliveryFee(
                                request.getStandardDeliveryFee()
                        )
                        .freeDeliveryAbove(
                                request.getFreeDeliveryAbove()
                        )
                        .averageDeliveryTimeMinutes(
                                request.getAverageDeliveryTimeMinutes()
                        )
                        .maxConcurrentOrders(
                                request.getMaxConcurrentOrders()
                        )
                        .upiEnabled(request.getUpiEnabled())
                        .cardEnabled(request.getCardEnabled())
                        .cashOnDeliveryEnabled(
                                request.getCashOnDeliveryEnabled()
                        )
                        .netBankingEnabled(
                                request.getNetBankingEnabled()
                        )
                        .newOrderReceived(
                                request.getNewOrderReceived()
                        )
                        .orderDelivered(
                                request.getOrderDelivered()
                        )
                        .lowStockAlert(
                                request.getLowStockAlert()
                        )
                        .newCustomerRegistered(
                                request.getNewCustomerRegistered()
                        )
                        .dailyReport(
                                request.getDailyReport()
                        )
                        .weeklyReport(
                                request.getWeeklyReport()
                        )
                        .deliveryPartners(
                                joinPartners(request.getDeliveryPartners())
                        )
                        .build();

        RestaurantSetting updatedSetting =
                restaurantSettingService.updateSettings(setting);

        return ResponseEntity.ok(
                mapToResponse(updatedSetting)
        );
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================

    private RestaurantSettingResponse mapToResponse(
            RestaurantSetting setting
    ) {

        return RestaurantSettingResponse.builder()
                .id(setting.getId())

                // General
                .restaurantName(setting.getRestaurantName())
                .ownerName(setting.getOwnerName())
                .email(setting.getEmail())
                .phone(setting.getPhone())
                .fssaiLicense(setting.getFssaiLicense())
                .gstNumber(setting.getGstNumber())
                .address(setting.getAddress())
                .openingTime(setting.getOpeningTime())
                .closingTime(setting.getClosingTime())

                // Delivery
                .deliveryRadiusKm(setting.getDeliveryRadiusKm())
                .minimumOrderAmount(
                        setting.getMinimumOrderAmount()
                )
                .standardDeliveryFee(
                        setting.getStandardDeliveryFee()
                )
                .freeDeliveryAbove(
                        setting.getFreeDeliveryAbove()
                )
                .averageDeliveryTimeMinutes(
                        setting.getAverageDeliveryTimeMinutes()
                )
                .maxConcurrentOrders(
                        setting.getMaxConcurrentOrders()
                )

                // Payments
                .upiEnabled(setting.getUpiEnabled())
                .cardEnabled(setting.getCardEnabled())
                .cashOnDeliveryEnabled(
                        setting.getCashOnDeliveryEnabled()
                )
                .netBankingEnabled(
                        setting.getNetBankingEnabled()
                )

                // Notifications
                .newOrderReceived(
                        setting.getNewOrderReceived()
                )
                .orderDelivered(
                        setting.getOrderDelivered()
                )
                .lowStockAlert(
                        setting.getLowStockAlert()
                )
                .newCustomerRegistered(
                        setting.getNewCustomerRegistered()
                )
                .dailyReport(
                        setting.getDailyReport()
                )
                .weeklyReport(
                        setting.getWeeklyReport()
                )

                // Delivery Partners
                .deliveryPartners(
                        splitPartners(setting.getDeliveryPartners())
                )

                // Timestamps
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())

                .build();
    }

    private String joinPartners(List<String> partners) {

        if (partners == null || partners.isEmpty()) {
            return null;
        }

        return partners.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private List<String> splitPartners(String partners) {

        if (partners == null || partners.isBlank()) {
            return List.of();
        }

        return Arrays.stream(partners.split(","))
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .collect(Collectors.toList());
    }
}
