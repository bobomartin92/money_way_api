package com.example.money_way.controller;

import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.response.AccountVerificationResponse;
import com.example.money_way.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/bills")
public class BillController {

    private final BillService billService;
    @PostMapping("/verify-account")
    public ResponseEntity<AccountVerificationResponse> VerifyElectricityAccount(@RequestBody AccountVerificationRequest request){
       return ResponseEntity.ok(billService.verifyElectricityAccount(request));
    }

}
