package com.example.money_way.controller;

import com.example.money_way.model.Bank;
import com.example.money_way.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/banks")
public class BankController {
    private final BankService bankListService;

    @GetMapping()
    public ResponseEntity<Page<Bank>> viewBalance(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(bankListService.getAllBanks(page, size));
    }
}
