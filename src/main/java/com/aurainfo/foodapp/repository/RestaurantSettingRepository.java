package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.RestaurantSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantSettingRepository
        extends JpaRepository<RestaurantSetting, Long> {

    Optional<RestaurantSetting> findTopByOrderByIdAsc();
}