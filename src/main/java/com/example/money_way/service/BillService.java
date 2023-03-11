package com.example.money_way.service;

import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.AirtimeRequest;
import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.request.ElectricityBillRequest;
import com.example.money_way.dto.response.*;


public interface BillService {
    ApiResponse buyData(DataPurchaseRequest request);

    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);
    
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

    VTPassResponse buyAirtime(AirtimeRequest airtimeRequest);
    ApiResponse payElectricityBill(ElectricityBillRequest request);

    ApiResponse<TvVariationsResponse> fetchTvVariations(String tvServiceProvider);
}
