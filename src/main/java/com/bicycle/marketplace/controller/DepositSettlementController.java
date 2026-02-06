package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.DepositSettlementCreationRequest;
import com.bicycle.marketplace.dto.request.DepositSettlementUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.DepositSettlementResponse;
import com.bicycle.marketplace.entities.DepositSettlement;
import com.bicycle.marketplace.services.DepositSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit-settlements")
public class DepositSettlementController {
    @Autowired
    private DepositSettlementService depositSettlementService;

    @PostMapping
    ApiResponse<DepositSettlementResponse> createDepositSettlement(@RequestBody DepositSettlementCreationRequest request) {
        ApiResponse<DepositSettlementResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositSettlementService.createDepositSettlement(request));
        apiResponse.setMessage("Deposit Settlement created successfully");
        return apiResponse;
    }

    @GetMapping("/{settlementId}")
    ApiResponse<DepositSettlementResponse> getDepositSettlementById(@PathVariable int settlementId) {
        ApiResponse<DepositSettlementResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositSettlementService.getDepositSettlementById(settlementId));
        apiResponse.setMessage("Deposit Settlement fetched successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<DepositSettlement>> getAllDepositSettlements() {
        ApiResponse<List<DepositSettlement>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositSettlementService.getAllDepositSettlements());
        apiResponse.setMessage("Deposit Settlements fetched successfully");
        return apiResponse;
    }

    @PutMapping("/{settlementId}")
    ApiResponse<DepositSettlementResponse> updateDepositSettlement(@PathVariable int settlementId, @RequestBody DepositSettlementUpdateRequest request) {
        ApiResponse<DepositSettlementResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositSettlementService.updateDepositSettlement(settlementId, request));
        apiResponse.setMessage("Deposit Settlement updated successfully");
        return apiResponse;
    }

    @DeleteMapping("/{settlementId}")
    ApiResponse<String> deleteDepositSettlement(@PathVariable int settlementId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(depositSettlementService.deleteDepositSettlement(settlementId));
        return apiResponse;
    }
}
