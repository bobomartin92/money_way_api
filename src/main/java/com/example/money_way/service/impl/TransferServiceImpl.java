package com.example.money_way.service.impl;

import com.example.money_way.dto.request.TransferToBankDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.TransferFeeResponse;
import com.example.money_way.dto.response.TransferToBankResponse;
import com.example.money_way.enums.Status;
import com.example.money_way.enums.TransactionType;
import com.example.money_way.exception.InvalidCredentialsException;
import com.example.money_way.exception.InvalidTransactionException;
import com.example.money_way.model.User;
import com.example.money_way.model.Wallet;
import com.example.money_way.model.Transaction;
import com.example.money_way.model.Transfer;
import com.example.money_way.model.Beneficiary;
import com.example.money_way.repository.TransactionRepository;
import com.example.money_way.repository.TransferRepository;
import com.example.money_way.repository.WalletRepository;
import com.example.money_way.repository.UserRepository;
import com.example.money_way.repository.BeneficiaryRepository;
import com.example.money_way.service.TransferService;
import com.example.money_way.utils.AppUtil;
import com.example.money_way.utils.RestTemplateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TransferServiceImpl implements TransferService {
    private final AppUtil appUtil;
    private final RestTemplateUtil restTemplateUtil;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<BigDecimal> getTransferFee(BigDecimal amount) {

        TransferFeeResponse feeResponse = restTemplateUtil.getTransferFeeFromFlutterwave(amount);

        ApiResponse<BigDecimal> apiResponse = new ApiResponse<>();
        assert feeResponse != null;
        apiResponse.setStatus(feeResponse.getStatus());
        apiResponse.setMessage(feeResponse.getMessage());
        apiResponse.setData(BigDecimal.valueOf((double) feeResponse.getData().get(0).get("fee")));

        return apiResponse;
    }

    @Override
    @Transactional
    public ApiResponse transferToBank(TransferToBankDto transferToBankDto) {

        User user = appUtil.getLoggedInUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow();

        BigDecimal fee = getTransferFee(transferToBankDto.getAmount()).getData();
        BigDecimal total = transferToBankDto.getAmount().add(fee);

        //Checking if user balance is greater than amount + fee
        if (wallet.getBalance().compareTo(total) < 0){
            throw new InvalidTransactionException("Insufficient wallet balance");
        }

        //Checking if user pin matches
        if (!passwordEncoder.matches(transferToBankDto.getPin(), user.getPin())) {
            throw new InvalidCredentialsException("Incorrect Pin");
        }

        //Generating a reference code
        String ref = appUtil.generateReference();

        TransferToBankResponse transferToBankResponse = restTemplateUtil.transferToBankWithFlutterwave(
                transferToBankDto, ref
        );

        //checking if transfer not queued successfully, then retry transfer
        if (transferToBankResponse != null && !transferToBankResponse.getStatus().equalsIgnoreCase("success")){

            transferToBankResponse = restTemplateUtil.retryTransferToBankWithFlutterwave(
                    transferToBankResponse);
        }

        //checking if transfer queued successfully
        if (transferToBankResponse != null && transferToBankResponse.getStatus().equalsIgnoreCase("success")){
            //Subtracting total from user wallet balance
            wallet.setBalance(wallet.getBalance().subtract(total));
            walletRepository.save(wallet);

            //Saving transfer to database
            Transfer transfer = new Transfer();
            transfer.setBankName(transferToBankDto.getAccount_bank());
            transfer.setAccountNumber(transferToBankDto.getAccount_number());
            transfer.setDescription(transferToBankDto.getDescription());
            transfer.setReferenceId(ref);
            transfer.setAmount(transferToBankDto.getAmount());
            transfer.setUserId(user.getId());
            transferRepository.save(transfer);

            //Saving transaction to database
            Transaction transaction = new Transaction();
            transaction.setTransactionId(Long.valueOf((Integer) transferToBankResponse.getData().get("id")));
            transaction.setCurrency((String) transferToBankResponse.getData().get("currency"));
            transaction.setAmount(BigDecimal.valueOf((Integer) transferToBankResponse.getData().get("amount")));
            transaction.setVirtualAccountRef((String) transferToBankResponse.getData().get("reference"));
            transaction.setDescription((String) transferToBankResponse.getData().get("narration"));
            transaction.setStatus(Status.PENDING);
            transaction.setResponseMessage(transferToBankResponse.getMessage());
            transaction.setProviderStatus((String) transferToBankResponse.getData().get("status"));
            transaction.setPaymentType("BANK");
            transaction.setUserId(user.getId());
            transactionRepository.save(transaction);

            //Checking if user wants to save beneficiary
            if (transferToBankDto.getSaveBeneficiary()){
                Optional<Beneficiary> beneficiary = beneficiaryRepository
                        .findByAccountNumberAndUserId(transferToBankDto.getAccount_number(), user.getId());

                if (beneficiary.isEmpty()){
                    Beneficiary newBeneficiary = new Beneficiary();
                    newBeneficiary.setName(transferToBankDto.getBeneficiaryName());
                    newBeneficiary.setBankName(transferToBankDto.getAccount_bank());
                    newBeneficiary.setAccountNumber(transfer.getAccountNumber());
                    newBeneficiary.setTransactionType(TransactionType.BANK);
                    newBeneficiary.setUserId(user.getId());

                    beneficiaryRepository.save(newBeneficiary);
                }
            }
        }

        ApiResponse<Map<String, ?>> apiResponse = new ApiResponse<>();
        assert transferToBankResponse != null;
        apiResponse.setStatus("PENDING");
        apiResponse.setMessage(transferToBankResponse.getMessage());

        return apiResponse;
    }

    @Override
    @Transactional
    public void updateTransferToBankResponse(TransferToBankResponse transferToBankResponse) {
        Map<String, ?> data = transferToBankResponse.getData();

        //fetching transaction to be updated
        Transaction transaction = transactionRepository
                .findByVirtualAccountRef((String) data.get("reference"))
                .orElseThrow();

        //checking if transaction is successful and updating status
        if (data.get("status").equals("SUCCESSFUL")){
            transaction.setStatus(Status.SUCCESS);
        } else {
            //adding amount and fee back to user wallet and updating status when transaction not successful
            User user = userRepository.findById(transaction.getUserId())
                    .orElseThrow();
            Wallet wallet = walletRepository.findByUserId(user.getId())
                    .orElseThrow();

            BigDecimal fee = BigDecimal.valueOf((double) data.get("fee"));
            BigDecimal amount = BigDecimal.valueOf((Integer) data.get("amount"));
            BigDecimal total = amount.add(fee);

            wallet.setBalance(wallet.getBalance().add(total));
            walletRepository.save(wallet);

            transaction.setStatus(Status.FAILED);
        }

        //updating transaction status and message from provider depending on status
        transaction.setProviderStatus((String) data.get("status"));
        transaction.setResponseMessage((String) data.get("complete_message"));
        transactionRepository.save(transaction);
    }
import com.example.money_way.dto.request.LocalTransferDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.enums.Type;
import com.example.money_way.exception.BeneficiaryAlreadyExistsException;
import com.example.money_way.exception.ValidationException;
import com.example.money_way.model.Transfer;
import com.example.money_way.model.Beneficiary;
import com.example.money_way.model.User;
import com.example.money_way.model.Wallet;
import com.example.money_way.repository.UserRepository;
import com.example.money_way.repository.WalletRepository;
import com.example.money_way.repository.BeneficiaryRepository;
import com.example.money_way.repository.TransferRepository;
import com.example.money_way.service.TransferService;
import com.example.money_way.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AppUtil appUtil;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransferRepository transferRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    @Override
    public ApiResponse localTransfer(LocalTransferDto localTransfer) {

        User user = appUtil.getLoggedInUser();

        Long userId = user.getId();
        Optional<Wallet> wallet1 = walletRepository.findByUserId(userId);

        if(!passwordEncoder.matches(localTransfer.getPin(), user.getPin()))
            throw new ValidationException("Pin is Incorrect!");

        if(wallet1.get().getBalance().compareTo(localTransfer.getAmount()) < 0)
           throw new UnsupportedOperationException("Insufficient funds!");

        Optional<User> receiver = userRepository.findByEmail(localTransfer.getEmail());

        Long receiverId = receiver.get().getId();

        Optional<Wallet> wallet2 = walletRepository.findById(receiverId);

        if(localTransfer.isSaveBeneficiary()) {

            Optional<Beneficiary> beneficiary = Optional.ofNullable(beneficiaryRepository.findByEmailAndUserId(localTransfer.getEmail(), userId));

            if (beneficiary.isPresent()) {
                throw new BeneficiaryAlreadyExistsException("Beneficiary already exists");
            }
            Beneficiary beneficiary1 = new Beneficiary();
            beneficiary1.setEmail(localTransfer.getEmail());
            beneficiary1.setName(receiver.get().getFirstName());
            beneficiary1.setPhoneNumber(receiver.get().getPhoneNumber());
            beneficiary1.setType(Type.LOCAL);
            beneficiary1.setUserId(userId);
            beneficiaryRepository.save(beneficiary1);
        }
        wallet1.get().setBalance((wallet1.get().getBalance().subtract(localTransfer.getAmount())));
        walletRepository.save(wallet1.get());

        wallet2.get().setBalance(wallet2.get().getBalance().add(localTransfer.getAmount()));
        walletRepository.save(wallet2.get());

        Transfer transfer = new Transfer();
        transfer.setAmount(localTransfer.getAmount());
        transfer.setEmail(localTransfer.getEmail());
        transfer.setDescription(localTransfer.getDescription());
        transfer.setUserId(userId);
        transfer.setReferenceId(appUtil.getReference());
        transferRepository.save(transfer);

        return new ApiResponse("Successful", "Transaction completed successfully", wallet1) ;
    }

}
