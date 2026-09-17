package com.aurainfo.foodapp.dto.response;

import com.aurainfo.foodapp.entity.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String name;
    private String email;
    private UserRole role;
}
