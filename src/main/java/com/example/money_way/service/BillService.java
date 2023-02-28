package com.example.money_way.service;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;
import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.response.AccountVerificationResponse;

public interface BillService {
    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);
    
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

}
