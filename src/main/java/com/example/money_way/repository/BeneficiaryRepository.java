package com.example.money_way.repository;

import com.example.money_way.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
     Beneficiary findByEmailAndUserId (String email, Long Id);

}
