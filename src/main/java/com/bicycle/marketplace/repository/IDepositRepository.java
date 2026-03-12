package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Deposit;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDepositRepository extends JpaRepository<Deposit, Integer> {
    List<Deposit> findAllByStatus(String status);
    Optional<Deposit> findByUserAndListing(Users user, BikeListing listing);
    List<Deposit> findByUser_UserIdOrderByCreatedAtDesc(int userId);
}
