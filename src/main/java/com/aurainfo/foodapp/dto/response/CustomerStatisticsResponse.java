package com.aurainfo.foodapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerStatisticsResponse {

    private Long customerId;

    private String name;

    private String email;

    private String phone;

    private String city;

    private long totalOrders;

    private BigDecimal totalSpent;

    private LocalDateTime joinedAt;

    private Boolean active;
}