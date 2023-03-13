package com.example.money_way.dto.request;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CreateTransactionPinDto {
    @NotNull
    private String oldPin;
    @NotNull
    private String newPin;
    @NotNull
    private String confirmPin;

}
