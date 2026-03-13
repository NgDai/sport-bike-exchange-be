package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Transaction;
import com.bicycle.marketplace.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t FROM Transaction t WHERE t.buyer = :user OR t.seller = :user")
    List<Transaction> findByBuyerOrSeller(@Param("user") Users user);

    List<Transaction> findAllByStatus(String status);

    Optional<Transaction> findByDeposit_DepositId(Integer depositId);

    Optional<Transaction> findByReservation_ReservationId(Integer reservationId);
}
