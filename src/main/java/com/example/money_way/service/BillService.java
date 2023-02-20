package com.example.money_way.service;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataResponse;
import com.example.money_way.enums.DataServiceProvider;

public interface BillService {
    ApiResponse<DataResponse> fetchDataVariations(String dataServiceProvider);
//    ApiResponse<DataResponse> fetchAirtelDataVariations();
//    ApiResponse<DataResponse> fetchGloDataVariations();
//    ApiResponse<DataResponse> fetch9MobileDataVariations();
}
