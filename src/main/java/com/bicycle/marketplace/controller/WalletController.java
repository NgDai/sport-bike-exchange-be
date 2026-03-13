package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.WalletAddBalanceRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.WalletResponse;
import com.bicycle.marketplace.dto.response.WalletTransactionResponse;
import com.bicycle.marketplace.services.WalletService;
import com.bicycle.marketplace.services.WalletTransactionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@Log
public class WalletController {

    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletTransactionService walletTransactionService;

    @GetMapping
    public ApiResponse<WalletResponse> getWallet() {
        ApiResponse<WalletResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(walletService.viewWallet());
        return apiResponse;
    }

    @PutMapping("/add")
    public ApiResponse<WalletTransactionResponse> addFund(@RequestBody WalletAddBalanceRequest amount) {
        ApiResponse<WalletTransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(walletService.addFunds(amount));
        return apiResponse;
    }

    @PutMapping("/withdraw")
    public ApiResponse<WalletTransactionResponse> withdrawFund(@RequestBody WalletAddBalanceRequest amount) {
        ApiResponse<WalletTransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(walletService.withdrawFunds(amount));
        return apiResponse;
    }

}
