package com.example.money_way.controller;

import com.example.money_way.dto.request.LocalTransferDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/local-transfer")
    public ApiResponse localTransfer(@Valid @RequestBody LocalTransferDto localTransfer)  {
        return transferService.localTransfer(localTransfer);
    }

}
