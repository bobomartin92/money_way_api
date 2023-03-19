package com.example.money_way.repository;

import com.example.money_way.dto.response.FinancialSummaryResponse;
import com.example.money_way.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByVirtualAccountRef(String ref);


    Optional<Transaction> findByTransactionId(Long transactionId);

    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) AS month,"
            + " SUM(CASE WHEN paymentType = 'Deposits' THEN amount ELSE 0 END) AS deposits_sum,"
            + " SUM(CASE WHEN paymentType <> 'Deposits' THEN amount ELSE 0 END) AS expense"
            + " FROM public.transaction"
            + " GROUP BY month"
            + " ORDER BY month DESC", nativeQuery = true)
    List<FinancialSummaryResponse> getTransactionsByMonth(@Param("userId") Long userId);


}
