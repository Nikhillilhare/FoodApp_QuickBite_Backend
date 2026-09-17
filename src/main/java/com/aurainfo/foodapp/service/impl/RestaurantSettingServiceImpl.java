package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.RestaurantSetting;
import com.aurainfo.foodapp.repository.RestaurantSettingRepository;
import com.aurainfo.foodapp.service.AuditLogService;
import com.aurainfo.foodapp.service.RestaurantSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantSettingServiceImpl
        implements RestaurantSettingService {

    private final RestaurantSettingRepository restaurantSettingRepository;
    private final AuditLogService auditLogService;
    @Override
    @Transactional(readOnly = true)
    public RestaurantSetting getSettings() {

        return restaurantSettingRepository
                .findTopByOrderByIdAsc()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Restaurant settings are not configured"
                        )
                );
    }

    @Override
    public RestaurantSetting createSettings(
            RestaurantSetting setting
    ) {

        validateSetting(setting);

        if (restaurantSettingRepository.count() > 0) {
            throw new IllegalStateException(
                    "Restaurant settings are already configured"
            );
        }

        normalizeSetting(setting);

        RestaurantSetting savedSetting =
                restaurantSettingRepository.save(setting);

        auditLogService.logCurrentAdminAction(
                "CREATE_RESTAURANT_SETTINGS",
                "RestaurantSetting",
                savedSetting.getId()
        );

        return savedSetting;
    }

    @Override
    public RestaurantSetting updateSettings(
            RestaurantSetting updatedSetting
    ) {

        validateSetting(updatedSetting);

        RestaurantSetting existingSetting = getSettings();

        // =========================
        // General
        // =========================

        existingSetting.setRestaurantName(
                updatedSetting.getRestaurantName().trim()
        );

        existingSetting.setOwnerName(
                updatedSetting.getOwnerName().trim()
        );

        existingSetting.setEmail(
                updatedSetting.getEmail().trim()
        );

        existingSetting.setPhone(
                updatedSetting.getPhone().trim()
        );

        existingSetting.setFssaiLicense(
                trimNullable(updatedSetting.getFssaiLicense())
        );

        existingSetting.setGstNumber(
                trimNullable(updatedSetting.getGstNumber())
        );

        existingSetting.setAddress(
                trimNullable(updatedSetting.getAddress())
        );

        existingSetting.setOpeningTime(
                updatedSetting.getOpeningTime()
        );

        existingSetting.setClosingTime(
                updatedSetting.getClosingTime()
        );

        // =========================
        // Delivery
        // =========================

        existingSetting.setDeliveryRadiusKm(
                updatedSetting.getDeliveryRadiusKm()
        );

        existingSetting.setMinimumOrderAmount(
                updatedSetting.getMinimumOrderAmount()
        );

        existingSetting.setStandardDeliveryFee(
                updatedSetting.getStandardDeliveryFee()
        );

        existingSetting.setFreeDeliveryAbove(
                updatedSetting.getFreeDeliveryAbove()
        );

        existingSetting.setAverageDeliveryTimeMinutes(
                updatedSetting.getAverageDeliveryTimeMinutes()
        );

        existingSetting.setMaxConcurrentOrders(
                updatedSetting.getMaxConcurrentOrders()
        );

        // =========================
        // Payments
        // =========================

        existingSetting.setUpiEnabled(
                updatedSetting.getUpiEnabled()
        );

        existingSetting.setCardEnabled(
                updatedSetting.getCardEnabled()
        );

        existingSetting.setCashOnDeliveryEnabled(
                updatedSetting.getCashOnDeliveryEnabled()
        );

        existingSetting.setNetBankingEnabled(
                updatedSetting.getNetBankingEnabled()
        );

        // =========================
        // Notifications
        // =========================

        existingSetting.setNewOrderReceived(
                updatedSetting.getNewOrderReceived()
        );

        existingSetting.setOrderDelivered(
                updatedSetting.getOrderDelivered()
        );

        existingSetting.setLowStockAlert(
                updatedSetting.getLowStockAlert()
        );

        existingSetting.setNewCustomerRegistered(
                updatedSetting.getNewCustomerRegistered()
        );

        existingSetting.setDailyReport(
                updatedSetting.getDailyReport()
        );

        existingSetting.setWeeklyReport(
                updatedSetting.getWeeklyReport()
        );

        existingSetting.setDeliveryPartners(
                updatedSetting.getDeliveryPartners()
        );

        RestaurantSetting savedSetting =
                restaurantSettingRepository.save(existingSetting);

        auditLogService.logCurrentAdminAction(
                "UPDATE_RESTAURANT_SETTINGS",
                "RestaurantSetting",
                savedSetting.getId()
        );

        return savedSetting;    }

    private void validateSetting(
            RestaurantSetting setting
    ) {

        if (setting == null) {
            throw new IllegalArgumentException(
                    "Restaurant settings cannot be null"
            );
        }

        if (isBlank(setting.getRestaurantName())) {
            throw new IllegalArgumentException(
                    "Restaurant name is required"
            );
        }

        if (isBlank(setting.getOwnerName())) {
            throw new IllegalArgumentException(
                    "Owner name is required"
            );
        }

        if (isBlank(setting.getEmail())) {
            throw new IllegalArgumentException(
                    "Restaurant email is required"
            );
        }

        if (isBlank(setting.getPhone())) {
            throw new IllegalArgumentException(
                    "Restaurant phone is required"
            );
        }

        if (setting.getDeliveryRadiusKm() == null ||
                setting.getDeliveryRadiusKm().signum() < 0) {

            throw new IllegalArgumentException(
                    "Delivery radius cannot be negative"
            );
        }

        if (setting.getMinimumOrderAmount() == null ||
                setting.getMinimumOrderAmount().signum() < 0) {

            throw new IllegalArgumentException(
                    "Minimum order amount cannot be negative"
            );
        }

        if (setting.getStandardDeliveryFee() == null ||
                setting.getStandardDeliveryFee().signum() < 0) {

            throw new IllegalArgumentException(
                    "Standard delivery fee cannot be negative"
            );
        }

        if (setting.getFreeDeliveryAbove() != null &&
                setting.getFreeDeliveryAbove().signum() < 0) {

            throw new IllegalArgumentException(
                    "Free delivery threshold cannot be negative"
            );
        }

        if (setting.getAverageDeliveryTimeMinutes() == null ||
                setting.getAverageDeliveryTimeMinutes() <= 0) {

            throw new IllegalArgumentException(
                    "Average delivery time must be greater than zero"
            );
        }

        if (setting.getMaxConcurrentOrders() == null ||
                setting.getMaxConcurrentOrders() <= 0) {

            throw new IllegalArgumentException(
                    "Maximum concurrent orders must be greater than zero"
            );
        }

        if (setting.getOpeningTime() != null &&
                setting.getClosingTime() != null &&
                setting.getOpeningTime().equals(
                        setting.getClosingTime()
                )) {

            throw new IllegalArgumentException(
                    "Opening and closing time cannot be the same"
            );
        }
    }

    private void normalizeSetting(
            RestaurantSetting setting
    ) {

        setting.setRestaurantName(
                setting.getRestaurantName().trim()
        );

        setting.setOwnerName(
                setting.getOwnerName().trim()
        );

        setting.setEmail(
                setting.getEmail().trim()
        );

        setting.setPhone(
                setting.getPhone().trim()
        );

        setting.setFssaiLicense(
                trimNullable(setting.getFssaiLicense())
        );

        setting.setGstNumber(
                trimNullable(setting.getGstNumber())
        );

        setting.setAddress(
                trimNullable(setting.getAddress())
        );
    }

    private String trimNullable(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(String value) {

        return value == null || value.isBlank();
    }
}