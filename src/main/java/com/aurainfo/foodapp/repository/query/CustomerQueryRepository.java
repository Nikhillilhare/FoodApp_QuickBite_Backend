package com.aurainfo.foodapp.repository.query;

import com.aurainfo.foodapp.dto.response.CustomerStatisticsResponse;

import java.util.List;
import java.util.Optional;

public interface CustomerQueryRepository {

    List<CustomerStatisticsResponse> findAllCustomerStatistics();

    Optional<CustomerStatisticsResponse> findCustomerStatisticsById(
            Long customerId
    );
}