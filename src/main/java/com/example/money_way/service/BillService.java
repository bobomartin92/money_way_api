package com.example.money_way.service;

import com.example.money_way.dto.request.CableVerificationRequest;
import com.example.money_way.dto.request.ElectricityBillRequest;
import com.example.money_way.dto.response.*;
import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;

public interface BillService {
    ApiResponse buyData(DataPurchaseRequest request);

    AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request);

    CableVerificationResponse verifyCableTv(CableVerificationRequest request);
    
    ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider);

    ApiResponse payElectricityBill(ElectricityBillRequest request);

}
