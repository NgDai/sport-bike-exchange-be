package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IWalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByUsername(String username);
}
