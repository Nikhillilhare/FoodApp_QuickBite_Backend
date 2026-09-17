package com.aurainfo.foodapp.controller;

import com.aurainfo.foodapp.dto.request.ForgotPasswordRequest;
import com.aurainfo.foodapp.dto.request.LoginRequest;
import com.aurainfo.foodapp.dto.request.RefreshTokenRequest;
import com.aurainfo.foodapp.dto.request.ResendOtpRequest;
import com.aurainfo.foodapp.dto.request.ResetPasswordRequest;
import com.aurainfo.foodapp.dto.request.SignupRequest;
import com.aurainfo.foodapp.dto.request.VerifyOtpRequest;
import com.aurainfo.foodapp.dto.response.AuthResponse;
import com.aurainfo.foodapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ==========================================
    // SIGNUP
    // ==========================================

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        authService.signup(request);

        return ResponseEntity.ok(
                "Signup successful. Please verify your email with the OTP."
        );
    }

    // ==========================================
    // VERIFY SIGNUP OTP
    // ==========================================

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        authService.verifySignupOtp(request);

        return ResponseEntity.ok(
                "Email verified successfully"
        );
    }

    // ==========================================
    // LOGIN
    // ==========================================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    // ==========================================
    // FORGOT PASSWORD
    // ==========================================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                "If the email is registered, a password reset OTP has been sent."
        );
    }

    // ==========================================
    // RESET PASSWORD
    // ==========================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                "Password reset successfully"
        );
    }

    // ==========================================
    // RESEND OTP
    // ==========================================

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {

        authService.resendOtp(request);

        return ResponseEntity.ok(
                "OTP sent successfully"
        );
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        return ResponseEntity.ok(
                "Logout successful. Please discard the access and refresh tokens."
        );
    }

    // ==========================================
    // REFRESH TOKEN
    // ==========================================

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                authService.refreshToken(request)
        );
    }
}