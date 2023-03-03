package com.example.money_way.service;

import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.response.AccountVerificationResponse;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataPurchaseResponse;
import com.example.money_way.dto.response.DataVariationsResponse;

public interface BillService {
    ApiResponse buyData(DataPurchaseRequest request);

    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);

    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

}
