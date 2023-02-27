package com.example.money_way.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class AirtimeRequestFE {
    private String requestId;
    private String serviceID;
    private String billersCode;
    private String variationCode;
    private BigDecimal amount;
    private String phoneNumber;
}
