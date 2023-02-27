package com.example.money_way.controller;

import com.example.money_way.dto.request.AirtimeRequestFE;
import com.example.money_way.dto.response.VTPassResponseDto;
import com.example.money_way.service.BillServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bills")
public class BillsController {

    private final BillServices billServices;

    @PostMapping("/buy-airtime")
    public ResponseEntity<VTPassResponseDto> buyAirtime(AirtimeRequestFE airtimeRequestFE) {
        return ResponseEntity.ok(billServices.buyAirtime(airtimeRequestFE));
    }
}
