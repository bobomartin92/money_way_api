package com.example.money_way.repository;

import com.example.money_way.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByVirtualAccountRef(String ref);
    Optional<Transaction> findByTransactionId(Long transactionId);
    @Query(value = "SELECT * FROM transaction_tbl t WHERE (t.user_id = :userId AND t.created_at BETWEEN cast(:startDate as Date) AND cast(:endDate as Date) + interval '1' day) ORDER BY t.created_at DESC LIMIT :limit_ OFFSET :offset_", nativeQuery = true)
    List<Transaction> findAllByUserId(Long userId, @Param("limit_") Integer limit,
                                      @Param("offset_") Integer offset,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate);

}
