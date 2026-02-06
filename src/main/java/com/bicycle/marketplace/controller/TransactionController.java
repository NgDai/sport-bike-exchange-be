package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.entities.Transaction;
import com.bicycle.marketplace.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping
    ApiResponse<TransactionResponse> createTransaction(@RequestBody TransactionCreationRequest request) {
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.createTransaction(request));
        apiResponse.setMessage("Transaction created successfully");
        return apiResponse;
    }

    @PutMapping("/{transactionId}")
    ApiResponse<TransactionResponse> updateTransaction(@PathVariable int transactionId, @RequestBody TransactionUpdateRequest request) {
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.updateTransaction(transactionId, request));
        apiResponse.setMessage("Transaction updated successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Transaction>> getAllTransactions() {
        ApiResponse<List<Transaction>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.findAllTransactions());
        apiResponse.setMessage("Transactions fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{transactionId}")
    ApiResponse<TransactionResponse> getTransactionById(@PathVariable int transactionId) {
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.findTransactionById(transactionId));
        apiResponse.setMessage("Transaction fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{transactionId}")
    ApiResponse<String> deleteTransaction(@PathVariable int transactionId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.deleteTransaction(transactionId));
        return apiResponse;
    }
}
