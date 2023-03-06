package com.example.money_way.service.impl;

import com.example.money_way.dto.request.*;
import com.example.money_way.dto.response.*;
import com.example.money_way.enums.Status;
import com.example.money_way.exception.InsufficientFundsException;
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
import java.math.MathContext;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    private final RestTemplate restTemplate;
    private final EnvironmentVariables environmentVariables;
    private final BeneficiaryRepository beneficiaryRepository;

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    private final RestTemplateUtil restTemplateUtil;
    private final AppUtil appUtil;



    @Override
    public ApiResponse<TvPurchaseResponse> purchaseTvSubscription(CustomerRequestDtoForTvSubscription request) {
        String transactionReference = appUtil.getReference() + "TV-SUBSCRIPTION";
        VtPassTvPurchaseRequest vtPassRequest = VtPassTvPurchaseRequest.builder()
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

            HttpEntity<VtPassTvPurchaseRequest> entity = new HttpEntity<>(vtPassRequest, headers);
            TvPurchaseResponse tvPurchaseResponse = restTemplate.exchange(environmentVariables.getPurchaseSubscriptionUrl(),
                    HttpMethod.POST, entity, TvPurchaseResponse.class).getBody();
//            if (request.isSaveBeneficiary()) {
//                saveBeneficiary(request, userId);
//            }
            wallet.setBalance(walletBalance.subtract(request.getAmount(),new MathContext(2)));
            walletRepository.save(wallet);
            saveTransactionForTvSubscription(request,transactionReference,userId);
            return new ApiResponse<>("Success", "Successful Transaction", tvPurchaseResponse);
        }
        return new ApiResponse<>("Failed","Insufficient Wallet Balance",null);
    }

//    private void saveBeneficiary(CustomerRequestDtoForTvSubscription request, Long userId) {
//        Optional<Beneficiary> savedBeneficiary = beneficiaryRepository.findBySmartCardNumber(request.getDecoderOrSmartCardNumber());
//        if (savedBeneficiary.isEmpty()) {
//            Beneficiary beneficiary = Beneficiary.builder()
//                    .userId(userId)
//                    .phoneNumber(request.getPhone())
//                    .SmartCardNumber(request.getDecoderOrSmartCardNumber())
//                    .build();
//            beneficiaryRepository.save(beneficiary);
//        }
//    }

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

    @Override
    public ApiResponse payElectricityBill(ElectricityBillRequest request){

        String requestId = appUtil.getReference();
        ElectricityRequestDto requestDto = ElectricityRequestDto.builder()
                .request_id(requestId)
                .serviceID(request.getServiceID())
                .billersCode(request.getBillersCode())
                .variation_code(request.getVariationCode())
                .amount(request.getAmount())
                .phone(request.getPhoneNumber())
                .build();

        User user = appUtil.getLoggedInUser();
        Long userId = user.getId();

        if (request.isSaveBeneficiary()){
            Optional<Beneficiary> savedBeneficiary = beneficiaryRepository.findByMeterNumber(request.getBillersCode());
            if(savedBeneficiary.isEmpty()){
                saveBeneficiary(request, userId);
            }
        }

        Wallet userWallet = walletRepository.findByUserId(userId).get();

        if (userWallet.getBalance().compareTo(BigDecimal.valueOf(Long.parseLong(request.getAmount()))) < 0){
            throw new InsufficientFundsException("Insufficient funds");
        }

        BillResponse billResponse = restTemplateUtil.getVTPassElectricityBillResponse(requestDto);

        if (billResponse.getCode().equals("000")){
            userWallet.setBalance(userWallet.getBalance().subtract(new BigDecimal(request.getAmount())));
            walletRepository.save(userWallet);
        }

        saveTransaction(requestId, userId, userWallet, billResponse);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(billResponse.getCode().equals("000") ? "SUCCESS" : "FAILED");
        apiResponse.setMessage(billResponse.getResponse_description());

        return apiResponse;
    }

    private void saveTransaction(String requestId, Long userId, Wallet userWallet, BillResponse billResponse) {
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .transactionId(Long.valueOf(requestId))
                .currency("NGN")
                .amount(BigDecimal.valueOf(Double.parseDouble(billResponse.getAmount())))
                .status(billResponse.getCode().equals("000") ? Status.SUCCESS : Status.FAILED)
                .virtualAccountRef(userWallet.getVirtualAccountRef())
                .build();
        transactionRepository.save(transaction);
    }

    private void saveBeneficiary(ElectricityBillRequest request, Long userId) {
        Beneficiary beneficiary = Beneficiary.builder()
                .userId(userId)
                .meterNumber(request.getBillersCode())
                .build();
        beneficiaryRepository.save(beneficiary);
    }
}
