package com.example.money_way.service;

import com.example.money_way.dto.request.TransferDto;
import com.example.money_way.dto.response.ApiResponse;


public interface TransferService {

    ApiResponse localTransfer (TransferDto localTransfer);

}
