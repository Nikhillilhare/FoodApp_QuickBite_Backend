package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.entity.OtpPurpose;

public interface OtpService {

    void generateAndSendOtp(String email, OtpPurpose purpose);

    void verifyOtp(String email, String otpCode, OtpPurpose purpose);
}
