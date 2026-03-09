package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.Wallet;
import com.bicycle.marketplace.entities.WalletTransaction;
import com.bicycle.marketplace.repository.IWalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletTransactionService {

    IWalletTransactionRepository walletTransactionRepository;

    public WalletTransaction createTransaction(
            Wallet wallet,
            double amount,
            String type,
            String description) {

        WalletTransaction transaction = new WalletTransaction();

        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);

        return walletTransactionRepository.save(transaction);
    }
}
