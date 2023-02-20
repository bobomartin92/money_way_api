package com.example.money_way.controller;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataResponse;
import com.example.money_way.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bills")
public class BillController {
    private final BillService billService;

    @GetMapping("/data-Variations/{dataServiceProvider}")
    public ResponseEntity<ApiResponse<DataResponse>> getDataVariations(@PathVariable String dataServiceProvider) {
        return ResponseEntity.ok(billService.fetchDataVariations(dataServiceProvider));
    }

}