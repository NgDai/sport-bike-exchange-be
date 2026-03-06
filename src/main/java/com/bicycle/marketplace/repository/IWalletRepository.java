package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWalletRepository extends JpaRepository<Wallet, Integer> {
}
