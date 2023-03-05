package com.example.money_way.service.impl;

import com.example.money_way.dto.request.AccountVerificationRequest;
import com.example.money_way.dto.request.CustomerRequestDtoForTvSubscription;
import com.example.money_way.dto.request.TvPurchaseRequest;
import com.example.money_way.dto.response.AccountVerificationResponse;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.DataVariationsResponse;
import com.example.money_way.dto.response.TvPurchaseResponse;
import com.example.money_way.exception.ResourceNotFoundException;
import com.example.money_way.model.Beneficiary;
import com.example.money_way.model.Transaction;
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
import java.math.MathContext;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    private final RestTemplate restTemplate;
    private final EnvironmentVariables environmentVariables;
    private final BeneficiaryRepository beneficiaryRepository;

    private final AppUtil appUtil;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    private final RestTemplateUtil restTemplateUtil;


    @Override
    public ApiResponse<TvPurchaseResponse> purchaseTvSubscription(CustomerRequestDtoForTvSubscription request) {
        String transactionReference = appUtil.getReference() + "TV-SUBSCRIPTION";
        TvPurchaseRequest vtPassRequest = TvPurchaseRequest.builder()
                .phone(request.getPhone())
                .billersCode(request.getDecoderOrSmartCardNumber())
                .request_id(transactionReference)
                .subscription_type(request.getSubscriptionType())
                .serviceID(request.getDecoderName())
                .variation_code(request.getSubscriptionPackage())
                .build();

        Long userId = appUtil.getLoggedInUser().getId();
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Wallet Not Found"));
        BigDecimal walletBalance = wallet.getBalance();
            if (walletBalance.compareTo(request.getAmount()) >= 0) {

               HttpHeaders headers = restTemplateUtil.getVTPASS_Header();

            HttpEntity<TvPurchaseRequest> entity = new HttpEntity<>(vtPassRequest, headers);
            TvPurchaseResponse tvPurchaseResponse = restTemplate.exchange(environmentVariables.getPurchaseSubscriptionUrl(),
                    HttpMethod.POST, entity, TvPurchaseResponse.class).getBody();
            if (request.isSaveBeneficiary()) {
                saveBeneficiary(vtPassRequest, userId);
            }
            wallet.setBalance(walletBalance.subtract(request.getAmount(),new MathContext(2)));
            walletRepository.save(wallet);
            saveTransactionForTvSubscription(request,transactionReference,userId);
            return new ApiResponse<>("Success", "Successful Transaction", tvPurchaseResponse);
        }
        return new ApiResponse<>("Failed","Insufficient Wallet Balance",null);
    }

    private void saveBeneficiary(TvPurchaseRequest request, Long userId) {
        Optional<Beneficiary> savedBeneficiary = beneficiaryRepository.findBeneficiariesBybillersCodeAndUserId(request.getBillersCode(), userId);
        if (savedBeneficiary.isEmpty()) {
            Beneficiary beneficiary = Beneficiary.builder()
                    .userId(userId)
                    .phoneNumber(request.getPhone()).build();
            beneficiaryRepository.save(beneficiary);
        }
    }

    private void saveTransactionForTvSubscription(CustomerRequestDtoForTvSubscription request, String transactionReference, Long userId) {
        Transaction transaction = Transaction.builder()
                .userId(userId).currency("NIL")
                .request_id(transactionReference)
                .amount(request.getAmount()).build();
                transactionRepository.save(transaction);
     }

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
}
