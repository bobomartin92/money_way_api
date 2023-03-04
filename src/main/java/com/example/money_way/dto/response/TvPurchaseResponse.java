package com.example.money_way.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TvPurchaseResponse {
    private String code;
    private List<Map<String,?>> content;
}