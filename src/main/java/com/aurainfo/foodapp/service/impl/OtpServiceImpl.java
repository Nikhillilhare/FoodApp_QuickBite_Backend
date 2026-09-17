package com.aurainfo.foodapp.service.impl;

import com.aurainfo.foodapp.entity.OtpPurpose;
import com.aurainfo.foodapp.entity.OtpVerification;
import com.aurainfo.foodapp.repository.OtpVerificationRepository;
import com.aurainfo.foodapp.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;

    private final OtpVerificationRepository otpVerificationRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiry-minutes}")
    private long otpExpiryMinutes;

    @Value("${app.otp.max-request-per-minute}")
    private long maxRequestPerMinute;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void generateAndSendOtp(String email, OtpPurpose purpose) {

        String normalizedEmail = normalizeEmail(email);

        if (purpose == null) {
            throw new IllegalArgumentException("OTP purpose is required");
        }

        if (otpExpiryMinutes <= 0) {
            throw new IllegalStateException("OTP expiry configuration must be greater than zero");
        }

        if (maxRequestPerMinute <= 0) {
            throw new IllegalStateException("OTP rate-limit configuration must be greater than zero");
        }

        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long requestCount = otpVerificationRepository
                .countByIdentifierAndCreatedAtAfter(
                        normalizedEmail,
                        oneMinuteAgo
                );

        if (requestCount >= maxRequestPerMinute) {
            throw new IllegalStateException(
                    "Too many OTP requests. Please try again later."
            );
        }

        String otp = generateOtp();
        String otpHash = passwordEncoder.encode(otp);
        LocalDateTime expiresAt =
                LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        OtpVerification otpVerification = OtpVerification.builder()
                .identifier(normalizedEmail)
                .otpCodeHash(otpHash)
                .purpose(purpose)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        otpVerificationRepository.save(otpVerification);

        sendOtpEmail(normalizedEmail, otp, purpose);
    }

    @Override
    public void verifyOtp(
            String email,
            String otpCode,
            OtpPurpose purpose
    ) {

        String normalizedEmail = normalizeEmail(email);

        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalArgumentException("OTP code is required");
        }

        if (!otpCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("OTP must be exactly 6 digits");
        }

        if (purpose == null) {
            throw new IllegalArgumentException("OTP purpose is required");
        }

        OtpVerification otpVerification =
                otpVerificationRepository
                        .findTopByIdentifierAndPurposeOrderByCreatedAtDesc(
                                normalizedEmail,
                                purpose
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OTP not found. Please request a new OTP."
                                )
                        );

        if (Boolean.TRUE.equals(otpVerification.getIsUsed())) {
            throw new IllegalArgumentException(
                    "OTP has already been used. Please request a new OTP."
            );
        }

        if (otpVerification.getExpiresAt() == null
                || otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "OTP has expired. Please request a new OTP."
            );
        }

        if (!passwordEncoder.matches(
                otpCode,
                otpVerification.getOtpCodeHash()
        )) {
            throw new IllegalArgumentException(
                    "OTP does not match"
            );
        }

        otpVerification.setIsUsed(true);
        otpVerificationRepository.save(otpVerification);
    }

    private String generateOtp() {
        int bound = 1_000_000;
        int value = secureRandom.nextInt(bound);
        return String.format(Locale.ROOT, "%06d", value);
    }

    private void sendOtpEmail(
            String email,
            String otp,
            OtpPurpose purpose
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(
                purpose == OtpPurpose.SIGNUP
                        ? "FoodApp - Email Verification OTP"
                        : "FoodApp - Password Reset OTP"
        );
        message.setText(
                "Your FoodApp OTP is: " + otp
                        + "\n\nThis OTP expires in "
                        + otpExpiryMinutes
                        + " minutes."
                        + "\n\nDo not share this OTP with anyone."
        );

        mailSender.send(message);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
