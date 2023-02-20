package com.example.money_way.repository;

import com.example.money_way.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTxReferenceId(String tx_ref);

    Optional<Transaction> findByTransactionId(Long transactionId);
}
