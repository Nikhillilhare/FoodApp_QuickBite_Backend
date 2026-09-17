package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.RestaurantSetting;

public interface RestaurantSettingService {

    RestaurantSetting getSettings();

    RestaurantSetting createSettings(
            RestaurantSetting setting
    );

    RestaurantSetting updateSettings(
            RestaurantSetting updatedSetting
    );
}