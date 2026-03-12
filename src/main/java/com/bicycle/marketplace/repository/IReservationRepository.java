package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findAllByStatus(String status);
    List<Reservation> findByBuyer_UserIdOrderByReservedAtDesc(int userId);
    java.util.Optional<Reservation> findByDeposit_DepositId(int depositId);
}
