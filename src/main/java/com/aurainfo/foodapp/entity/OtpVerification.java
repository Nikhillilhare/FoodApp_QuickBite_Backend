package com.aurainfo.foodapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String identifier;

    @Column(name = "otp_code_hash", nullable = false, length = 255)
    private String  otpCodeHash;

    @Enumerated(EnumType.STRING)
    @Column(name="purpose", nullable = false)
    private OtpPurpose purpose;

    @Column(name = "is_used",nullable = false)
    @Builder.Default
    private Boolean isUsed=false;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="expires_at", nullable = false)
    private LocalDateTime expiresAt;

}
