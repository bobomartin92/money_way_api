package com.example.money_way.service.impl;

import com.example.money_way.dto.request.AirtimeRequestDto;
import com.example.money_way.dto.response.VTPassApiResponse;
import com.example.money_way.dto.response.VTPassResponseDto;
import com.example.money_way.enums.Status;
import com.example.money_way.model.Transaction;
import com.example.money_way.model.User;
import com.example.money_way.model.Wallet;
import com.example.money_way.repository.TransactionRepository;
import com.example.money_way.repository.WalletRepository;
import com.example.money_way.service.BillServices;
import com.example.money_way.utils.AppUtil;
import com.example.money_way.utils.EnvironmentVariables;
import com.example.money_way.utils.RestTemplateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillServicesImpl implements BillServices {

    private final EnvironmentVariables environmentVariables;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final RestTemplate restTemplate;
    private final AppUtil appUtil;
    private final RestTemplateUtil apiHeaders;


    @Override
    public VTPassResponseDto buyAirtime(AirtimeRequestDto airtimeRequestDto) {

        HttpEntity<AirtimeRequestDto> entity = new HttpEntity<>(airtimeRequestDto, apiHeaders.getVTPASS_Header());
        VTPassApiResponse vtPassApiResponse = restTemplate.exchange(environmentVariables.getBuy_airtime_endpoint(),
                HttpMethod.POST, entity, VTPassApiResponse.class).getBody();

        //Extracting the response payload
        VTPassResponseDto vtPassResponseDto = appUtil.getObjectMapper().convertValue(vtPassApiResponse.getContent(), VTPassResponseDto.class);

        if (vtPassResponseDto.getStatus().equalsIgnoreCase("delivered")) {
            updateWallet(appUtil.getLoggedInUser().getId(), vtPassResponseDto.getUnit_price());
        }
        saveTransaction(vtPassResponseDto);
        return vtPassResponseDto;
    }

    private void updateWallet(Long userId, double amount) {
        Wallet userWallet = walletRepository.findByUserId(userId).get();
        userWallet.setBalance(userWallet.getBalance().subtract(BigDecimal.valueOf(amount)));

        walletRepository.save(userWallet);
    }

    private void saveTransaction(VTPassResponseDto vtPassApiResponse) {
        transactionRepository.save(Transaction.builder()
                        .transactionId(Long.parseLong(vtPassApiResponse.getTransactionId()))
                        .amount(BigDecimal.valueOf(vtPassApiResponse.getUnit_price()))
                        .currency("NGN")
                        .description(vtPassApiResponse.getProduct_name())
                        .status(vtPassApiResponse.getStatus().equalsIgnoreCase("delivered") ? Status.SUCCESS : Status.FAILED)
                        .paymentType(vtPassApiResponse.getType())
                .build());
    }


}
