package com.example.money_way.service.impl;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.enums.TransactionType;
import com.example.money_way.model.Beneficiary;
import com.example.money_way.model.User;
import com.example.money_way.repository.BeneficiaryRepository;
import com.example.money_way.service.BeneficiaryService;
import com.example.money_way.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AppUtil appUtil;
    @Override
    public ApiResponse<List<Beneficiary>> getBeneficiaries(String transactionType) {
        User user = appUtil.getLoggedInUser();

        List<Beneficiary> beneficiaryList = beneficiaryRepository.findAllByUserId(user.getId());

        List<Beneficiary> response = new ArrayList<>();

        if (transactionType.equalsIgnoreCase("TRANSFER")){

            response = beneficiaryList.stream()
                    .filter(beneficiary ->
                            beneficiary.getTransactionType().equals(TransactionType.BANK) ||
                            beneficiary.getTransactionType().equals(TransactionType.LOCAL)
                            )
                    .collect(Collectors.toList());

        } else if (transactionType.equalsIgnoreCase("AIRTIME")){

            response = beneficiaryList.stream()
                    .filter(beneficiary ->
                            beneficiary.getTransactionType().equals(TransactionType.AIRTIME)
                    )
                    .collect(Collectors.toList());

        } else if (transactionType.equalsIgnoreCase("BILL")){

            response = beneficiaryList.stream()
                    .filter(beneficiary ->
                            beneficiary.getTransactionType().equals(TransactionType.BILL)
                    )
                    .collect(Collectors.toList());

        }

        ApiResponse<List<Beneficiary>> apiResponse = new ApiResponse<>();
        apiResponse.setStatus("SUCCESS");
        apiResponse.setMessage("Successfully Fetched Beneficiaries of User");
        apiResponse.setData(response);

        return apiResponse;
    }
}
