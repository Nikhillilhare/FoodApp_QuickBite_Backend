package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.dto.request.*;
import com.aurainfo.foodapp.dto.response.AuthResponse;
import com.aurainfo.foodapp.entity.OtpPurpose;
import com.aurainfo.foodapp.entity.User;
import com.aurainfo.foodapp.entity.UserRole;
import com.aurainfo.foodapp.repository.UserRepository;
import com.aurainfo.foodapp.security.JwtUtil;
import com.aurainfo.foodapp.service.AuthService;
import com.aurainfo.foodapp.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void signup(SignupRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Signup request cannot be null");
        }

        String email = normalizeEmail(request.getEmail());

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .phone(normalizePhone(request.getPhone()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .verified(false)
                .active(true)
                .build();

        userRepository.save(user);

        otpService.generateAndSendOtp(
                email,
                OtpPurpose.SIGNUP
        );
    }

    @Override
    public void verifySignupOtp(VerifyOtpRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "OTP verification request cannot be null"
            );
        }

        if (request.getPurpose() != OtpPurpose.SIGNUP) {
            throw new IllegalArgumentException(
                    "Only SIGNUP OTP can be used for email verification"
            );
        }

        String email = normalizeEmail(request.getEmail());

        otpService.verifyOtp(
                email,
                request.getOtpCode(),
                OtpPurpose.SIGNUP
        );

        User user = getUserByEmail(email);

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new IllegalStateException(
                    "Email is already verified"
            );
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        String email = normalizeEmail(request.getEmail());

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = getUserByEmail(email);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException("User account is inactive");
        }

        if (!Boolean.TRUE.equals(user.getVerified())) {
            throw new IllegalStateException(
                    "Please verify your email before logging in"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Forgot password request cannot be null"
            );
        }

        String email = normalizeEmail(request.getEmail());

        if (!userRepository.existsByEmail(email)) {
            return;
        }

        otpService.generateAndSendOtp(
                email,
                OtpPurpose.FORGOT_PASSWORD
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Reset password request cannot be null"
            );
        }

        String email = normalizeEmail(request.getEmail());

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "New password must be at least 8 characters"
            );
        }

        otpService.verifyOtp(
                email,
                request.getOtpCode(),
                OtpPurpose.FORGOT_PASSWORD
        );

        User user = getUserByEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    @Override
    public void resendOtp(ResendOtpRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Resend OTP request cannot be null"
            );
        }

        String email = normalizeEmail(request.getEmail());

        if (request.getPurpose() == null) {
            throw new IllegalArgumentException("OTP purpose is required");
        }

        User user = getUserByEmail(email);

        if (request.getPurpose() == OtpPurpose.SIGNUP) {
            if (Boolean.TRUE.equals(user.getVerified())) {
                throw new IllegalStateException("Email is already verified");
            }
        }

        otpService.generateAndSendOtp(
                email,
                request.getPurpose()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Refresh token request cannot be null"
            );
        }

        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException(
                    "Invalid or expired refresh token"
            );
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = getUserByEmail(email);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException("User account is inactive");
        }

        if (!Boolean.TRUE.equals(user.getVerified())) {
            throw new IllegalStateException(
                    "Please verify your email before using the account"
            );
        }

        String accessToken = jwtUtil.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No account found with this email"
                        )
                );
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim();
    }
}
