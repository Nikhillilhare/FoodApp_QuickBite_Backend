package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.dto.request.ChangePasswordRequest;
import com.aurainfo.foodapp.dto.request.UpdateProfileRequest;
import com.aurainfo.foodapp.dto.response.CustomerProfileResponse;
import com.aurainfo.foodapp.dto.response.CustomerStatisticsResponse;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.entity.UserRole;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.repository.query.CustomerQueryRepository;
import com.aurainfo.foodapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerQueryRepository customerQueryRepository;

    @Override
    public User createUser(User user) {

        validateUser(user);

        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        user.setEmail(email);

        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }

        if (user.getVerified() == null) {
            user.setVerified(false);
        }

        if (user.getActive() == null) {
            user.setActive(true);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        validateId(userId, "User ID");

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + userId
                ));
    }

    @Override
    public boolean setTwoFactorEnabled(Long userId, boolean enabled) {

        validateId(userId, "User ID");

        User user = getUserById(userId);
        user.setTwoFactorEnabled(enabled);

        User savedUser = userRepository.save(user);

        return Boolean.TRUE.equals(savedUser.getTwoFactorEnabled());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with email: " + normalizedEmail
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long userId, User updatedUser) {
        validateId(userId, "User ID");

        if (updatedUser == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }

        User existingUser = getUserById(userId);

        if (updatedUser.getName() != null &&
                !updatedUser.getName().isBlank()) {
            existingUser.setName(updatedUser.getName().trim());
        }

        if (updatedUser.getPhone() != null) {
            existingUser.setPhone(updatedUser.getPhone().trim());
        }

        /*
         * Email/password/role are intentionally not changed here.
         * Those operations belong to dedicated Auth/security flows.
         */

        return userRepository.save(existingUser);
    }

    @Override
    public User setUserActive(Long userId, boolean active) {
        User user = getUserById(userId);
        user.setActive(active);
        return userRepository.save(user);
    }

    @Override
    public User verifyUser(Long userId) {
        User user = getUserById(userId);
        user.setVerified(true);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("User name is required");
        }

        normalizeEmail(user.getEmail());

        if (user.getPasswordHash() == null ||
                user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return email.trim().toLowerCase();
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerStatisticsResponse> getAllCustomers() {

        return customerQueryRepository.findAllCustomerStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerStatisticsResponse getCustomerStatistics(
            Long customerId
    ) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be greater than zero"
            );
        }
        return customerQueryRepository.findCustomerStatisticsById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with id: "
                                        + customerId
                        )
                );
    }

    //Customer Part

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfile(Long userId) {

        User user = getUserById(userId);

        return mapToCustomerProfile(user);
    }

    @Override
    public CustomerProfileResponse updateCustomerProfile(
            Long userId,
            UpdateProfileRequest request
    ) {

        validateId(userId, "User ID");

        if (request == null) {
            throw new IllegalArgumentException(
                    "Profile data cannot be null"
            );
        }

        User user = getUserById(userId);

        if (request.getName() == null ||
                request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Name is required"
            );
        }

        if (request.getPhone() == null ||
                request.getPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "Phone is required"
            );
        }

        user.setName(request.getName().trim());
        user.setPhone(request.getPhone().trim());

        User savedUser = userRepository.save(user);

        return mapToCustomerProfile(savedUser);
    }

    //Password

    @Override
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        validateId(userId, "User ID");

        if (request == null) {
            throw new IllegalArgumentException(
                    "Password data cannot be null"
            );
        }

        User user = getUserById(userId);

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash()
        )) {

            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPasswordHash()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    //Custpomer Mapper method

    private CustomerProfileResponse mapToCustomerProfile(
            User user
    ) {

        return CustomerProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .verified(user.getVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
    }