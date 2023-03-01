package com.example.money_way.service.impl;

import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.request.DataRequestDto;
import com.example.money_way.dto.response.AccountVerificationResponse;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataPurchaseResponse;
import com.example.money_way.dto.response.DataVariationsResponse;
import com.example.money_way.enums.Status;
import com.example.money_way.exception.ResourceNotFoundException;
import com.example.money_way.model.Beneficiary;
import com.example.money_way.model.Transaction;
import com.example.money_way.model.User;
import com.example.money_way.model.Wallet;
import com.example.money_way.repository.BeneficiaryRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BillServiceImpl implements BillService {
    private final EnvironmentVariables environmentVariables;
    private final RestTemplate restTemplate;
    private final RestTemplateUtil restTemplateUtil;
    private final BeneficiaryRepository beneficiaryRepository;
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
    public ApiResponse<DataPurchaseResponse> buyData(DataPurchaseRequest request) {
        String transactionReference = appUtil.getReference()+"DATA-BUNDLE";
        DataRequestDto dataRequestDto = DataRequestDto.builder()
                .request_id(transactionReference)
                .serviceID(request.getServiceID())
                .billersCode(request.getBillersCode())
                .variation_code(request.getVariationCode())
                .amount(request.getAmount())
                .phone(request.getPhoneNumber())
                .build();

        User user = appUtil.getLoggedInUser();
        Long userId = user.getId();

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Wallet No Found"));
        BigDecimal walletBalance = wallet.getBalance();

        if (walletBalance.compareTo(request.getAmount()) >= 0){

            DataPurchaseResponse response = restTemplateUtil.getDataPurchaseResponse(dataRequestDto);

            if (request.isSaveBeneficiary()){
                saveBeneficiary(request, userId);
            }

            if (response.getCode().equals("000")){
                wallet.setBalance(BigDecimal.valueOf(walletBalance.doubleValue() - request.getAmount().doubleValue()));
                walletRepository.save(wallet);
            }

            saveTransaction(response, userId);

            return new ApiResponse<>("Success", "Successful Transaction", response);
        }

        return new ApiResponse<>("Failed", "Insufficient Wallet Balance", null);
    }

    private void saveBeneficiary(DataPurchaseRequest request, Long userId) {
        Optional<Beneficiary> savedBeneficiary = beneficiaryRepository.findBeneficiariesByPhoneNumber(request.getPhoneNumber());
        if (savedBeneficiary.isEmpty()) {
            Beneficiary beneficiary = Beneficiary.builder()
                    .userId(userId)
                    .phoneNumber(request.getPhoneNumber()).build();
            beneficiaryRepository.save(beneficiary);
        }

    }
    private void saveTransaction(DataPurchaseResponse response, Long userId) {
        Transaction transaction = Transaction.builder()
                .transactionId((Long) response.getContent().get(0).get("transactionId"))
                .userId(userId)
                .currency("NGN")
                .status(response.getCode().equals("000") ? Status.SUCCESS : Status.FAILED)
                .request_id(response.getRequestId())
                .amount(BigDecimal.valueOf(Double.parseDouble(response.getAmount())))
                .build();

        transactionRepository.save(transaction);
    }
}






