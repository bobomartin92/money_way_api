package com.example.money_way.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataPurchaseResponse {
    private String code;
    private List<Map<String, ?>> content;
    private String response_description;
    private String requestId;
    private String amount;
    private List<Map<String, ?>> transaction_date;
    private String purchased_code;
}