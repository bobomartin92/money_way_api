package com.example.money_way.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class DataPurchaseRequest {
    private String serviceID;
    private String billersCode;
    private String variationCode;
    private BigDecimal amount;
    private String phoneNumber;
    private boolean saveBeneficiary;
}
