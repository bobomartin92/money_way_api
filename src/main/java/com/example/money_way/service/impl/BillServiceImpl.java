package com.example.money_way.service.impl;

import com.example.money_way.dto.request.CreateWalletRequest;
import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.exception.ResourceNotFoundException;
import com.example.money_way.model.Beneficiary;
import com.example.money_way.model.User;
import com.example.money_way.model.Wallet;
import com.example.money_way.repository.BeneficiaryRepository;
import com.example.money_way.repository.WalletRepository;
import com.example.money_way.service.BillService;
import com.example.money_way.utils.AppUtil;
import com.example.money_way.utils.EnvironmentVariables;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final RestTemplate restTemplate;
    private final EnvironmentVariables environmentVariables;
    private final BeneficiaryRepository beneficiaryRepository;
    private final WalletRepository walletRepository;
    private final AppUtil appUtil;

    @Override
    public ApiResponse buyData(DataPurchaseRequest request) {

        User user = appUtil.getLoggedInUser();
        Long userId = user.getId();

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Wallet No Found"));
        BigDecimal walletBalance = wallet.getBalance();

        if (walletBalance.compareTo(request.getAmount()) > 0 || walletBalance.compareTo(request.getAmount()) == 0 ){

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + environmentVariables.getFLW_SECRET_KEY());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DataPurchaseRequest> entity = new HttpEntity<>(request, headers);

        wallet.setBalance(BigDecimal.valueOf(walletBalance.doubleValue() - request.getAmount().doubleValue()));
        walletRepository.save(wallet);

        if (request.isSaveBeneficiary()){
            Optional<Beneficiary> savedBeneficiary = beneficiaryRepository.findBeneficiariesByPhoneNumber(request.getCustomer());
            if(savedBeneficiary.isEmpty()){
                Beneficiary beneficiary = Beneficiary.builder()
                        .phoneNumber(request.getCustomer()).build();
                beneficiaryRepository.save(beneficiary);
            }
        }

        return restTemplate.exchange(environmentVariables.getDataPurchaseUrl(),
                HttpMethod.POST, entity, ApiResponse.class).getBody();
    }
        return new ApiResponse("Failed", "Insufficient Wallet Balance", null);
}
}
