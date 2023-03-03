package com.example.money_way.dto.request;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TvPurchaseRequest {
    private String request_id;
    private String serviceID;
    private String billersCode;
    private String variation_code;
    private BigDecimal amount;
    private String phone;
    private String subscription_type;
    private int quantity;
    private boolean saveBeneficiary;
}