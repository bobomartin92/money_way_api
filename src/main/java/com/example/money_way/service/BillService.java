package com.example.money_way.service;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;

public interface BillService {
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);
}
