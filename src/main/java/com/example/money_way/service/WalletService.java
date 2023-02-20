package com.example.money_way.service;

import com.example.money_way.dto.request.CreateWalletRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.ViewWalletResponseDto;

public interface WalletService {

    ApiResponse<ViewWalletResponseDto> viewBalance();
    ApiResponse createWallet(CreateWalletRequest request);



}
