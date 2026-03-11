package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IWalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUsername(String username);
}
