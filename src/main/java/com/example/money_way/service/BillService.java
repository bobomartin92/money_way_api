package com.example.money_way.service;

import com.example.money_way.dto.request.AirtimeRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;
import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.response.AccountVerificationResponse;
import com.example.money_way.dto.response.VTPassResponseDto;

public interface BillService {
    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);
    
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

    VTPassResponseDto buyAirtime(AirtimeRequest airtimeRequest);

}
