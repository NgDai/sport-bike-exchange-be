package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.DepositCreationRequest;
import com.bicycle.marketplace.dto.request.DepositUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.DepositResponse;
import com.bicycle.marketplace.entities.Deposit;
import com.bicycle.marketplace.services.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposits")
public class DepositController {
    @Autowired
    private DepositService depositService;

    @PostMapping("/{listingId}/create")
    ApiResponse<DepositResponse> createDeposit(@PathVariable int listingId, @RequestBody DepositCreationRequest request) {
        ApiResponse<DepositResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositService.createDeposit(listingId, request));
        apiResponse.setMessage("Deposit created successfully");
        return apiResponse;
    }

    @PostMapping("/{listingId}/create-vnpay")
    ApiResponse<com.bicycle.marketplace.dto.response.CreateDepositResponse> createDepositViaVNPay(@PathVariable int listingId) {
        ApiResponse<com.bicycle.marketplace.dto.response.CreateDepositResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositService.createDepositViaVNPay(listingId));
        apiResponse.setMessage("Tạo giao dịch đặt cọc thành công");
        return apiResponse;
    }

    @PostMapping("/confirm/{depositId}")
    public ApiResponse<String> confirmDepositPayment(@PathVariable int depositId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositService.confirmDepositPayment(depositId));
        return apiResponse;
    }

//    @PutMapping("/{depositId}")
//    ApiResponse<DepositResponse> updateDeposit(@PathVariable int depositId, @RequestBody DepositUpdateRequest request) {
//        ApiResponse<DepositResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(depositService.updateDeposit(depositId, request));
//        apiResponse.setMessage("Deposit updated successfully");
//        return apiResponse;
//    }
//
//    @GetMapping("/{depositId}")
//    ApiResponse<DepositResponse> getDepositById(@PathVariable int depositId) {
//        ApiResponse<DepositResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(depositService.findDepositById(depositId));
//        apiResponse.setMessage("Deposit fetched successfully");
//        return apiResponse;
//    }
//
//    @GetMapping
//    ApiResponse<List<Deposit>> getAllDeposits() {
//        ApiResponse<List<Deposit>> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(depositService.findAllDeposits());
//        apiResponse.setMessage("Deposits fetched successfully");
//        return apiResponse;
//    }
//
//    //transaction is deleted first, then deposit will be deleted
//    @DeleteMapping("/{depositId}")
//    ApiResponse<String> deleteDeposit(@PathVariable int depositId) {
//        ApiResponse<String> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(depositService.deleteDeposit(depositId));
//        return apiResponse;
//    }
//
//    @GetMapping("/status/{status}")
//    @PreAuthorize("hasRole('ADMIN')")
//    ApiResponse<List<Deposit>> getDepositsByStatus(@PathVariable String status) {
//        ApiResponse<List<Deposit>> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(depositService.findDepositsByStatus(status));
//        apiResponse.setMessage("Deposits fetched successfully");
//        return apiResponse;
//    }
}
