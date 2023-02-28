package com.example.money_way.service.impl;

import com.example.money_way.dto.request.AirtimeRequest;
import com.example.money_way.dto.request.AirtimeRequestDto;
import com.example.money_way.dto.response.*;
import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.CreateWalletRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.enums.Status;
import com.example.money_way.model.Transaction;
import com.example.money_way.model.Wallet;
import com.example.money_way.repository.TransactionRepository;
import com.example.money_way.repository.WalletRepository;
import com.example.money_way.service.BillService;
import com.example.money_way.utils.AppUtil;
import com.example.money_way.utils.EnvironmentVariables;
import com.example.money_way.utils.RestTemplateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class BillServiceImpl implements BillService {
    private final EnvironmentVariables environmentVariables;
    private final RestTemplate restTemplate;

    private final RestTemplateUtil restTemplateUtil;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AppUtil appUtil;

    @Override
    public AccountVerificationResponse verifyElectricityAccount(AccountVerificationRequest request) {
        HttpHeaders headers = restTemplateUtil.getVTPASS_Header();
        HttpEntity<AccountVerificationRequest> entity = new HttpEntity<>(request, headers);

        AccountVerificationResponse response = restTemplate.exchange(environmentVariables.getVerifyElectricityAccountUrl(),
                HttpMethod.POST, entity, AccountVerificationResponse.class).getBody();
        return response;
    }
    
    
     @Override
    public ApiResponse<DataVariationsResponse> fetchDataVariations(String dataServiceProvider) {
        DataVariationsResponse response = restTemplateUtil.fetchDataVariations(dataServiceProvider);
         return  new ApiResponse<>("Success", null, response);
    }

    @Override
    public VTPassResponseDto buyAirtime(AirtimeRequest airtimeRequest) {
        AirtimeRequestDto airtimeRequestDto = AirtimeRequestDto.builder()
                .request_id(airtimeRequest.getRequestId())
                .variation_code(airtimeRequest.getVariationCode())
                .serviceID(airtimeRequest.getServiceID())
                .phone(airtimeRequest.getPhoneNumber())
                .amount(airtimeRequest.getAmount())
                .billersCode(airtimeRequest.getBillersCode())
                .build();

        HttpEntity<AirtimeRequestDto> entity = new HttpEntity<>(airtimeRequestDto, restTemplateUtil.getVTPASS_Header());
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
