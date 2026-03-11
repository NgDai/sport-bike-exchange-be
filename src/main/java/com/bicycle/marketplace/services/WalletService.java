package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.ReceiveFundsRequest;
import com.bicycle.marketplace.dto.request.TransferFundsRequest;
import com.bicycle.marketplace.dto.request.WalletAddBalanceRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.WalletResponse;
import com.bicycle.marketplace.dto.response.WalletTransactionResponse;
import com.bicycle.marketplace.entities.Transaction;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wallet;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.WalletMapper;
import com.bicycle.marketplace.repository.ITransactionRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.repository.IWalletRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Builder
@Slf4j
public class WalletService {
    @Autowired
    IWalletRepository walletRepository;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    WalletTransactionService walletTransactionService;

    @Autowired
    ITransactionRepository transactionRepository;

    // public WalletResponse viewWallet(){
    // var context = SecurityContextHolder.getContext();
    // String name = context.getAuthentication().getName();
    //
    // var wallet = walletRepository.findByUsername(name).orElseThrow(() -> new
    // AppException(ErrorCode.WALLET_NOT_FOUND));
    //
    // return walletMapper.toWalletResponse(wallet);
    // }
    //
    // public WalletResponse addFunds(WalletAddBalanceRequest request){
    // var context = SecurityContextHolder.getContext();
    // String name = context.getAuthentication().getName();
    //
    // Wallet wallet = walletRepository.findByUsername(name).orElseThrow(() -> new
    // AppException(ErrorCode.WALLET_NOT_FOUND));
    //
    // wallet.setBalance(wallet.getBalance() + request.getAmount());
    // walletRepository.save(wallet);
    //
    // return walletMapper.toWalletResponse(wallet);
    // }

    public WalletResponse viewWallet() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // TỰ ĐỘNG TẠO VÍ NẾU CHƯA CÓ
        Wallet wallet = walletRepository.findByUsername(name).orElseGet(() -> {
            Users user = userRepository.findByUsername(name)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(name)
                    .balance(0.0)
                    .type("User")
                    .build();
            return walletRepository.save(newWallet);
        });

        return walletMapper.toWalletResponse(wallet);

    }

    @Transactional
    public WalletTransactionResponse addFunds(WalletAddBalanceRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // TỰ ĐỘNG TẠO VÍ NẾU CHƯA CÓ KHI NẠP TIỀN
        Wallet wallet = walletRepository.findByUsername(name).orElseGet(() -> {
            Users user = userRepository.findByUsername(name)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(name)
                    .balance(0.0)
                    .type("User")
                    .build();
            return walletRepository.save(newWallet);
        });

        // Cộng tiền vào ví
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        // Tạo Transaction và trả về Response tương ứng
        return walletTransactionService.createTransaction(wallet, request.getAmount(), "Deposit",
                "Added funds to wallet");
    }

    @Transactional
    public WalletTransactionResponse withdrawFunds(WalletAddBalanceRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Wallet wallet = walletRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (wallet.getBalance() < request.getAmount()) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        walletRepository.save(wallet);

        return walletTransactionService.createTransaction(wallet, request.getAmount(), "Withdrawal",
                "Withdrew funds from wallet");
    }

    @Transactional
    public WalletTransactionResponse transferFunds(TransferFundsRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Wallet senderWallet = walletRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (senderWallet.getBalance() < request.getAmount()) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        senderWallet.setBalance(senderWallet.getBalance() - request.getAmount());
        systemWallet.setBalance(systemWallet.getBalance() + request.getAmount());

        walletRepository.save(senderWallet);
        walletRepository.save(systemWallet);

        return walletTransactionService.createTransaction(senderWallet, request.getAmount(), request.getType(),
                request.getDescription());
    }

    @Transactional
    public WalletTransactionResponse receiveFunds(ReceiveFundsRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Wallet receiverWallet = walletRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (systemWallet.getBalance() < request.getAmount()) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        systemWallet.setBalance(systemWallet.getBalance() - request.getAmount());
        receiverWallet.setBalance(receiverWallet.getBalance() + request.getAmount());

        walletRepository.save(systemWallet);
        walletRepository.save(receiverWallet);

//        Transaction transaction = Transaction.builder()
//                .amount(request.getAmount())
//                .type(request.getType())
//                .description(request.getDescription())
//                .senderWallet(systemWallet)
//                .receiverWallet(receiverWallet)
//                .build();

        return walletTransactionService.createTransaction(receiverWallet, request.getAmount(), request.getType(),
                request.getDescription());
    }
}
