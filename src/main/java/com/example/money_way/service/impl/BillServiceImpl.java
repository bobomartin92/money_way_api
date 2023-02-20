package com.example.money_way.service.impl;

import com.example.money_way.dto.request.DataPurchaseRequest;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataPurchaseResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final RestTemplate restTemplate;
    private final EnvironmentVariables environmentVariables;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AppUtil appUtil;

    @Override
    public ApiResponse<DataPurchaseResponse> buyData(DataPurchaseRequest request) {
        String transactionReference = getReference();
        request.setRequest_id(transactionReference);

        User user = appUtil.getLoggedInUser();
        Long userId = user.getId();

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Wallet No Found"));
        BigDecimal walletBalance = wallet.getBalance();

        if (walletBalance.compareTo(request.getAmount()) >= 0){

        HttpHeaders headers = new HttpHeaders();
        headers.add("api-key", environmentVariables.getVtPassApiKey() );
        headers.add("secret-key",environmentVariables.getVtPassSecretKey() );
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DataPurchaseRequest> entity = new HttpEntity<>(request, headers);

            DataPurchaseResponse dataPurchaseResponse = restTemplate.exchange(environmentVariables.getPurchaseDataUrl(),
                HttpMethod.POST, entity, DataPurchaseResponse.class).getBody();

            if (request.isSaveBeneficiary()){
                saveBeneficiary(request, userId);
            }

        wallet.setBalance(BigDecimal.valueOf(walletBalance.doubleValue() - request.getAmount().doubleValue()));
            walletRepository.save(wallet);
        saveTransaction(request, transactionReference, userId);

        return new ApiResponse<>("Success", "Successful Transaction", dataPurchaseResponse);
    }

        return new ApiResponse("Failed", "Insufficient Wallet Balance", null);
    }

    private String getReference() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateString = now.format(formatter);
        return dateString+"DATA-BUNDLE";
    }

    private void saveBeneficiary(DataPurchaseRequest request, Long userId) {
        Optional<Beneficiary> savedBeneficiary = beneficiaryRepository.findBeneficiariesByPhoneNumber(request.getPhone());
        if (savedBeneficiary.isEmpty()) {
            Beneficiary beneficiary = Beneficiary.builder()
                    .userId(userId)
                    .phoneNumber(request.getPhone()).build();
            beneficiaryRepository.save(beneficiary);
        }

    }
    private void saveTransaction(DataPurchaseRequest request, String transactionReference, Long userId) {
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .currency("NIL")
                .transactionId(transactionReference)
                .amount(request.getAmount())
                .build();
        transactionRepository.save(transaction);
    }
}




