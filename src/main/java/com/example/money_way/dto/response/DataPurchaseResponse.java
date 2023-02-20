package com.example.money_way.dto.response;

import lombok.Data;

@Data
public class DataPurchaseResponse<T> {
    private String code;
    private T content;
}
//    private String phone_number;
//    private String amount;
//    private String network;
//    private String flw_ref;
//    private String tx_ref;
//    private String reference;