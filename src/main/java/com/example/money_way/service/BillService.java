package com.example.money_way.service;

import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.request.TransactionStatusRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataPurchaseResponse;

public interface BillService {
    ApiResponse<DataPurchaseResponse> buyData(DataPurchaseRequest request);
}
