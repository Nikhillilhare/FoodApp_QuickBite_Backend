package com.aurainfo.foodapp.service;

import com.aurainfo.foodapp.dto.request.*;
import com.aurainfo.foodapp.dto.response.AuthResponse;

public interface AuthService {

    void signup(SignupRequest request);

    void verifySignupOtp(VerifyOtpRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void resendOtp(ResendOtpRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
