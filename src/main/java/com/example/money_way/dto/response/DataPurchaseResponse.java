package com.example.money_way.dto.response;

import lombok.Data;

@Data
public class DataPurchaseResponse<T> {
    private String code;
    private T content;
}