package com.example.money_way.service;

import com.example.money_way.dto.request.AirtimeRequest;
import com.example.money_way.dto.response.*;

import com.example.money_way.dto.request.ElectricityBillRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;

import com.example.money_way.dto.request.AccountVerificationRequest;

public interface BillService {
    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);
    
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

    VTPassResponse buyAirtime(AirtimeRequest airtimeRequest);
    ApiResponse payElectricityBill(ElectricityBillRequest request);


}
