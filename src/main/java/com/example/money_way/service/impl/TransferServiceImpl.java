package com.example.money_way.service.impl;

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
