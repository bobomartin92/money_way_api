package com.example.money_way.service.impl;

import com.example.money_way.dto.request.LocalTransferDto;
import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.enums.Type;
import com.example.money_way.exception.ValidationException;
import com.example.money_way.model.*;
import com.example.money_way.repository.*;
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
    public ApiResponse localTransfer(LocalTransferDto localTransfer) throws ValidationException {

        User user = appUtil.getLoggedInUser();

        Optional<Wallet> wallet1 = walletRepository.findByUserId(user.getId());

        if(wallet1.get().getBalance().compareTo(localTransfer.getAmount()) < 0)
           throw new UnsupportedOperationException("Insufficient funds!");

        if(!passwordEncoder.matches(localTransfer.getPin(), user.getPin()))
           throw new ValidationException("Pin is Incorrect!");

        Optional<User> receiver = userRepository.findIdByEmail(localTransfer.getEmail());

        if(receiver.isPresent()) {

            Optional<Wallet> wallet2 = walletRepository.findByUserId(receiver.get().getId());

            wallet1.get().setBalance((wallet1.get().getBalance().subtract(localTransfer.getAmount())));
            walletRepository.save(wallet1.get());

            wallet2.get().setBalance(wallet2.get().getBalance().add(localTransfer.getAmount()));
            walletRepository.save(wallet2.get());
        }

        Beneficiary beneficiary = new Beneficiary();

        if(localTransfer.getSaveBeneficiary().equals(true)) {

                beneficiary.setEmail(localTransfer.getEmail());
                beneficiary.setName(receiver.get().getFirstName());
                beneficiary.setPhoneNumber(receiver.get().getPhoneNumber());
                beneficiary.setType(Type.LOCAL);
                beneficiary.setBankName("MoneyWay");
                beneficiary.setUserId(user.getId());
                beneficiaryRepository.save(beneficiary);
            }

        Transfer transfer = new Transfer();
        transfer.setAmount(localTransfer.getAmount());
        transfer.setEmail(localTransfer.getEmail());
        transfer.setDescription(localTransfer.getDescription());
        transfer.setUserId(appUtil.getLoggedInUser().getId());
        transfer.setReferenceId(appUtil.getReference());
        transfer.setBankName("MoneyWay");
        transferRepository.save(transfer);

        return new ApiResponse("Successful", "Transaction completed successfully", wallet1) ;
    }

}
