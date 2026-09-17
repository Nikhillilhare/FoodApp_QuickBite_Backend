package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.ChangePasswordRequest;
import com.aurainfo.foodapp.dto.response.TwoFactorStatusResponse;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
public class AdminSecurityController {

    private final UserService userService;

    // =====================================================
    // CHANGE PASSWORD (ADMIN)
    // =====================================================

    @PatchMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {

        Long userId = getAuthenticatedUserId(authentication);

        userService.changePassword(userId, request);

        return ResponseEntity.ok("Password changed successfully");
    }

    // =====================================================
    // TWO-FACTOR AUTHENTICATION STATUS
    // =====================================================

    @GetMapping("/2fa")
    public ResponseEntity<TwoFactorStatusResponse> getTwoFactorStatus(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                TwoFactorStatusResponse.builder()
                        .enabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
                        .build()
        );
    }

    @PatchMapping("/2fa")
    public ResponseEntity<TwoFactorStatusResponse> setTwoFactorStatus(
            @RequestBody TwoFactorStatusResponse request,
            Authentication authentication
    ) {

        Long userId = getAuthenticatedUserId(authentication);

        boolean enabled = userService.setTwoFactorEnabled(
                userId,
                Boolean.TRUE.equals(request.getEnabled())
        );

        return ResponseEntity.ok(
                TwoFactorStatusResponse.builder()
                        .enabled(enabled)
                        .build()
        );
    }

    // =====================================================
    // AUTHENTICATED ADMIN
    // =====================================================

    private User getAuthenticatedUser(Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated admin could not be identified"
            );
        }

        return userService.getUserByEmail(authentication.getName());
    }

    private Long getAuthenticatedUserId(Authentication authentication) {

        return getAuthenticatedUser(authentication).getId();
    }
}
