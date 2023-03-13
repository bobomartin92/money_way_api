package com.example.money_way.repository;

import com.example.money_way.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByVirtualAccountRef(String ref);
    Optional<Transaction> findByTransactionId(Long transactionId);
    List<Transaction> findByUserId(Long userId);
}
