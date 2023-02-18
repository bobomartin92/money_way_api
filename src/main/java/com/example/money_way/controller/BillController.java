package com.example.money_way.controller;

import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bills")
public class BillController {
    private final BillService billService;

    @PostMapping("/buy-data")
    public ResponseEntity<ApiResponse> buyData(@Valid @RequestBody DataPurchaseRequest request){
        return ResponseEntity.ok(billService.buyData(request));
    }
}
