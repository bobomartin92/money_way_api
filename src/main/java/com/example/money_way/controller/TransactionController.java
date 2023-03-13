package com.example.money_way.controller;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.TransactionLogResponse;
import com.example.money_way.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("transactions")
    public ResponseEntity<ApiResponse<List<TransactionLogResponse>>> viewTransactions(@RequestParam int pageNo,
                                                                                      @RequestParam int pageSize){
        return ResponseEntity.ok(transactionService.viewTransactionLog(pageNo, pageSize));
    }
}
