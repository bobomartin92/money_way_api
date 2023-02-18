package com.example.money_way.service;

import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.response.ApiResponse;

public interface BillService {
    ApiResponse buyData(DataPurchaseRequest request);
}
