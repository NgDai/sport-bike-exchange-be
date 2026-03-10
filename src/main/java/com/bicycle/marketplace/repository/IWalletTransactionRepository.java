package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {

    List<WalletTransaction> findByWallet_WalletId(int walletId);
}
