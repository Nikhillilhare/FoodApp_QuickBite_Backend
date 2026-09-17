package com.aurainfo.foodapp.repository;

import com.aurainfo.foodapp.entity.OtpPurpose;
import com.aurainfo.foodapp.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long> {
    Optional<OtpVerification> findTopByIdentifierAndPurposeOrderByCreatedAtDesc(String identifier, OtpPurpose purpose);
    long countByIdentifierAndCreatedAtAfter(String identifier, LocalDateTime after);
}
