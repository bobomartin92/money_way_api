package com.example.money_way.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DataPurchaseRequest {
    @NotNull(message = "This field cannot be empty")
    private String country;
    @NotNull(message = "This field cannot be empty, Please enter destination phone number")
    private String customer;
    @NotNull(message = "This field cannot be empty")
    private BigDecimal amount;
    @NotNull(message = "This field cannot be empty")
    private String type;
    @NotNull(message = "This field cannot be empty")
    private String reference;
    private boolean saveBeneficiary;
}
