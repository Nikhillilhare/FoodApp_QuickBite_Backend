package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.ChangePasswordRequest;
import com.aurainfo.foodapp.dto.request.UpdateProfileRequest;
import com.aurainfo.foodapp.dto.response.CustomerProfileResponse;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    // =====================================================
    // GET CUSTOMER PROFILE
    // =====================================================

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> getProfile(
            Authentication authentication
    ) {

        Long userId =
                getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                userService.getCustomerProfile(userId)
        );
    }

    // =====================================================
    // UPDATE CUSTOMER PROFILE
    // =====================================================

    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {

        Long userId =
                getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                userService.updateCustomerProfile(
                        userId,
                        request
                )
        );
    }

    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {

        Long userId =
                getAuthenticatedUserId(authentication);

        userService.changePassword(
                userId,
                request
        );

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }

    // =====================================================
    // AUTHENTICATED USER
    // =====================================================

    private Long getAuthenticatedUserId(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated customer could not be identified"
            );
        }

        return userService
                .getUserByEmail(authentication.getName())
                .getId();
    }
}