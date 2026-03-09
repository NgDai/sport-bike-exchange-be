package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.TransactionResponse;
import com.bicycle.marketplace.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ApiResponse<TransactionResponse> createTransaction(@RequestBody @Valid TransactionCreationRequest request) {
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.createTransaction(request));
        apiResponse.setMessage("Transaction created successfully");
        return apiResponse;
    }

    @PutMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<TransactionResponse> updateTransaction(@PathVariable int transactionId,
            @RequestBody TransactionUpdateRequest request) {
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.updateTransaction(transactionId, request));
        apiResponse.setMessage("Transaction updated successfully");
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<TransactionResponse>> getAllTransactions() {
        ApiResponse<List<TransactionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.findAllTransactionResponses());
        apiResponse.setMessage("Transactions fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<TransactionResponse> getTransactionById(@PathVariable int transactionId) {
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.findTransactionById(transactionId));
        apiResponse.setMessage("Transaction fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteTransaction(@PathVariable int transactionId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.deleteTransaction(transactionId));
        return apiResponse;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        ApiResponse<List<Transaction>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(transactionService.findTransactionsByStatus(status));
        apiResponse.setMessage("Transactions fetched successfully");
        return apiResponse;
    }
}
