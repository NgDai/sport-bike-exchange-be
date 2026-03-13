package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.response.WalletTransactionResponse;
import com.bicycle.marketplace.entities.Wallet;
import com.bicycle.marketplace.entities.WalletTransaction;
import com.bicycle.marketplace.mapper.WalletTransactionMapper;
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
    WalletTransactionMapper walletTransactionMapper;

    public WalletTransactionResponse createTransaction(
            Wallet wallet,
            double amount,
            String type,
            String description) {

        WalletTransaction transaction = new WalletTransaction();

        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        walletTransactionRepository.save(transaction);
        transaction.setBalance(wallet.getBalance());

        return walletTransactionMapper.toWalletTransactionResponse(walletTransactionRepository.save(transaction));
    }
}
