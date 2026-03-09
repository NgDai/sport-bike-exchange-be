package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findAllByStatus(String status);

    Optional<Transaction> findByDeposit_DepositId(Integer depositId);
}
