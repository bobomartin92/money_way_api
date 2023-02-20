package com.example.money_way.service;

import com.example.money_way.model.Bank;
import org.springframework.data.domain.Page;

public interface BankService {

    Page<Bank> getAllBanks(int page, int size);

    void updateBankList();
}
