package com.example.money_way.service;

import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.TvPurchaseRequest;
import com.example.money_way.dto.response.AccountVerificationResponse;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;
import com.example.money_way.dto.response.TvPurchaseResponse;

public interface BillService {
    ApiResponse<TvPurchaseResponse> purchaseTvSubscription(TvPurchaseRequest tvPurchaseRequest);


    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);
    
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

}
