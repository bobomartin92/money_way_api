package com.example.money_way.service.impl;

import com.example.money_way.dto.request.*;
import com.example.money_way.dto.response.*;
import com.example.money_way.enums.Status;
import com.example.money_way.exception.InsufficientFundsException;
import com.example.money_way.exception.InvalidCredentialsException;
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

import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final AppUtil appUtil;
    private final WalletRepository walletRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;


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
    public VTPassResponse buyAirtime(AirtimeRequest airtimeRequest) {
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
        VTPassApiContent vtPassApiContent = appUtil.getObjectMapper().convertValue(vtPassApiResponse.getContent(), VTPassApiContent.class);
        VTPassResponseDto vtPassResponseDto = appUtil.getObjectMapper().convertValue(vtPassApiContent.getTransactions(), VTPassResponseDto.class);

        //Update wallet only when transaction is successful
        if (vtPassResponseDto.getStatus().equalsIgnoreCase("delivered")) {
            updateWallet(appUtil.getLoggedInUser().getId(), vtPassResponseDto.getUnit_price());
        }
        saveTransaction(vtPassResponseDto, vtPassApiResponse.getRequestId());

        return VTPassResponse.builder()
                .productName(vtPassResponseDto.getProduct_name())
                .uniqueElement(vtPassResponseDto.getUnique_element())
                .unitPrice(vtPassResponseDto.getUnit_price())
                .email(vtPassResponseDto.getEmail())
                .phoneNumber(vtPassResponseDto.getPhone())
                .quantity(vtPassResponseDto.getQuantity())
                .status(vtPassResponseDto.getStatus())
                .transactionId(vtPassResponseDto.getTransactionId())
                .type(vtPassResponseDto.getType())
                .build();
    }

    private void updateWallet(Long userId, double amount) {
        Wallet userWallet = walletRepository.findByUserId(userId).get();
        userWallet.setBalance(userWallet.getBalance().subtract(BigDecimal.valueOf(amount)));

        walletRepository.save(userWallet);
    }

    private void saveTransaction(VTPassResponseDto vtPassApiResponse, String requestId) {
        transactionRepository.save(Transaction.builder()
                .transactionId(Long.parseLong(requestId))
                .userId(appUtil.getLoggedInUser().getId())
                .amount(BigDecimal.valueOf(vtPassApiResponse.getUnit_price()))
                .currency("NGN")
                .description(vtPassApiResponse.getProduct_name())
                .status(vtPassApiResponse.getStatus().equalsIgnoreCase("delivered") ? Status.SUCCESS :
                        vtPassApiResponse.getStatus().equalsIgnoreCase("pending") ||
                                vtPassApiResponse.getStatus().equalsIgnoreCase("initiated") ? Status.PENDING : Status.FAILED)
                .paymentType(vtPassApiResponse.getType())
                .build());
    }
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

    @Override
    public ApiResponse buyData(DataPurchaseRequest request) {
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

        if (!passwordEncoder.matches(request.getPin(), user.getPin())) {
            throw new InvalidCredentialsException("Incorrect Pin");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("Wallet Not Found"));
        BigDecimal walletBalance = wallet.getBalance();


        if (walletBalance.compareTo(request.getAmount()) >= 0){

            DataPurchaseResponse response = restTemplateUtil.getDataPurchaseResponse(dataRequestDto);

            if (request.isSaveBeneficiary()){
                saveBeneficiary(request, userId);
            }

            if (response.getCode().equals("000")){
                wallet.setBalance(walletBalance.subtract(request.getAmount()));
                walletRepository.save(wallet);
            }

            saveTransaction(response, userId);

            ApiResponse apiResponse = new ApiResponse<>();
            apiResponse.setStatus("SUCCESS");
            apiResponse.setMessage(response.getResponse_description());

            return apiResponse;
        }

        throw new InsufficientFundsException("Insufficient Funds");
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
//                .transactionId((Long) response.getContent().get("transactionId"))
                .userId(userId)
                .currency("NGN")
                .status(response.getCode().equals("000") ? Status.valueOf("SUCCESS") : Status.valueOf("FAILED"))
                .request_id(response.getRequestId())
                .amount(BigDecimal.valueOf(Double.parseDouble(response.getAmount())))
                .build();

        transactionRepository.save(transaction);
    }
}
