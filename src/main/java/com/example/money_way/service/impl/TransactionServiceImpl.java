package com.example.money_way.service.impl;

import com.example.money_way.dto.response.ApiResponse;
import com.example.money_way.dto.response.TransactionLogResponse;
import com.example.money_way.model.Transaction;
import com.example.money_way.model.User;
import com.example.money_way.repository.TransactionRepository;
import com.example.money_way.service.TransactionService;
import com.example.money_way.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AppUtil appUtil;

    @Override
    public ApiResponse<List<TransactionLogResponse>> viewTransactionLog(int pageNo, int pageSize) {
        User user = appUtil.getLoggedInUser();
        Long userId = user.getId();

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());

        List<Transaction> transactionList = new ArrayList<>(transactions).subList(start, end);

        if (transactionList.isEmpty()){
            return new ApiResponse<>("Success", "You have no transaction", null);
        }

        List<TransactionLogResponse> transactionLogResponseList = new ArrayList<>();
        for (Transaction transaction : transactionList){
            mapToResponse(transaction, transactionLogResponseList);
        }
        return ApiResponse.<List<TransactionLogResponse>>builder()
                .data(transactionLogResponseList)
                .build();
    }

    private static void mapToResponse(Transaction transaction,
                                      List<TransactionLogResponse> transactionLogResponseList) {
        TransactionLogResponse transactionLogResponse = TransactionLogResponse.builder()
                .description(transaction.getDescription())
                .currency(transaction.getCurrency())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .paymentType(transaction.getPaymentType())
                .date(transaction.getCreatedAt())
                .requestId(transaction.getRequest_id())
                .virtualAccountRef(transaction.getVirtualAccountRef())
                .build();
        transactionLogResponseList.add(transactionLogResponse);
    }
}
