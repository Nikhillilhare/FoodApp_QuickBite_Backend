package com.aurainfo.foodapp.dto.response;

import com.aurainfo.foodapp.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileResponse {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private UserRole role;

    private Boolean verified;

    private Boolean active;

    private LocalDateTime createdAt;
}