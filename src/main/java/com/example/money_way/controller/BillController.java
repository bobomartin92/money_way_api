package com.example.money_way.controller;


import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.AirtimeRequest;
import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.request.ElectricityBillRequest;
import com.example.money_way.dto.response.*;
import com.example.money_way.dto.webhook.VTPassWebhookResponse;
import com.example.money_way.service.BillService;
import com.example.money_way.service.VTPassWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/bills")
public class BillController {

    private final BillService billService;
    private final VTPassWebhookService vtPassWebhookService;
    @PostMapping("/verify-account")
    public ResponseEntity<AccountVerificationResponse> VerifyElectricityAccount(@RequestBody AccountVerificationRequest request){
       return ResponseEntity.ok(billService.verifyElectricityAccount(request));
    }

     @GetMapping("/data-Variations/{dataServiceProvider}")
    public ResponseEntity<ApiResponse<DataVariationsResponse>> getDataVariations(@PathVariable String dataServiceProvider) {
        return ResponseEntity.ok(billService.fetchDataVariations(dataServiceProvider));
    }

    @PostMapping("/buy-airtime")
    public ResponseEntity<VTPassResponse> buyAirtime(AirtimeRequest airtimeRequest) {
        return ResponseEntity.ok(billService.buyAirtime(airtimeRequest));
    }

    @PostMapping("/buy-data")
    public ResponseEntity<ApiResponse> buyData(@Valid @RequestBody DataPurchaseRequest request){
        return ResponseEntity.ok(billService.buyData(request));
    }

    @PostMapping("/purchase-EKEDC-electricity")
    public ResponseEntity<ApiResponse> purchaseElectricityEKEDC(@RequestBody ElectricityBillRequest electricityRequest) {
        ApiResponse response = billService.payElectricityBill(electricityRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/bills-webhook-vtpass")
    public ResponseEntity<VTPassWebhookResponse> processWebHook(@RequestBody VTPassApiResponse vtPassApiResponse) {
        return vtPassWebhookService.billsWebhookHandler(vtPassApiResponse);
    }

    @PostMapping("/tv-variations/{tvServiceProvider}")
    public ResponseEntity<ApiResponse<TvVariationsResponse>> getTvVariations(@PathVariable String tvServiceProvider) {
        return ResponseEntity.ok(billService.fetchTvVariations(tvServiceProvider));
    }
}
