package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.WalletAddBalanceRequest;
import com.bicycle.marketplace.dto.response.WalletResponse;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wallet;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.WalletMapper;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.repository.IWalletRepository;
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

//    public WalletResponse viewWallet(){
//        var context = SecurityContextHolder.getContext();
//        String name = context.getAuthentication().getName();
//
//        var wallet = walletRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
//
//        return walletMapper.toWalletResponse(wallet);
//    }
//
//    public WalletResponse addFunds(WalletAddBalanceRequest request){
//        var context = SecurityContextHolder.getContext();
//        String name = context.getAuthentication().getName();
//
//        Wallet wallet = walletRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
//
//        wallet.setBalance(wallet.getBalance() + request.getAmount());
//        walletRepository.save(wallet);
//
//        return walletMapper.toWalletResponse(wallet);
//    }

    public WalletResponse viewWallet(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // TỰ ĐỘNG TẠO VÍ NẾU CHƯA CÓ
        Wallet wallet = walletRepository.findByUsername(name).orElseGet(() -> {
            Users user = userRepository.findByUsername(name)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(name)
                    .balance(0.0) // Số dư mặc định bằng 0
                    .build();
            return walletRepository.save(newWallet);
        });

        return walletMapper.toWalletResponse(wallet);
    }

    public WalletResponse addFunds(WalletAddBalanceRequest request){
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
                    .build();
            return walletRepository.save(newWallet);
        });

        // Cộng tiền vào ví
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        walletRepository.save(wallet);

        return walletMapper.toWalletResponse(wallet);
    }
}
