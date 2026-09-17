package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.dto.request.ChangePasswordRequest;
import com.aurainfo.foodapp.dto.request.UpdateProfileRequest;
import com.aurainfo.foodapp.dto.response.CustomerProfileResponse;
import com.aurainfo.foodapp.dto.response.CustomerStatisticsResponse;
import com.aurainfo.foodapp.entity.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User getUserById(Long userId);

   User getUserByEmail(String email);

    List<User> getAllUsers();

    User updateUser(Long userId, User updatedUser);

    User setUserActive(Long userId, boolean active);

    User verifyUser(Long userId);

    void deleteUser(Long userId);

    List<CustomerStatisticsResponse> getAllCustomers();

    CustomerStatisticsResponse getCustomerStatistics(Long customerId);

    CustomerProfileResponse getCustomerProfile(Long userId);

    CustomerProfileResponse updateCustomerProfile(
            Long userId,
            UpdateProfileRequest request
    );

    boolean setTwoFactorEnabled(Long userId, boolean enabled);

    void changePassword(
            Long userId,
            ChangePasswordRequest request
    );
}
