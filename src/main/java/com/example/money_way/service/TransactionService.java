package com.example.money_way.service;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.TransactionLogResponse;

import java.util.List;

public interface TransactionService {
    ApiResponse<List<TransactionLogResponse>> viewTransactionLog(int pageNo, int pageSize);
}
