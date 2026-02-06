package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.services.DisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disputes")
public class DisputeController {
    @Autowired
    private DisputeService disputeService;

    @PostMapping
    ApiResponse<DisputeResponse> createDispute(@RequestBody DisputeCreationRequest request){
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.createDispute(request));
        apiResponse.setMessage("Dispute created successfully");
        return apiResponse;
    }

    @PutMapping("/{disputeId}")
    ApiResponse<DisputeResponse> updateDispute(@PathVariable int disputeId, @RequestBody DisputeUpdateRequest request){
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.updateDispute(disputeId, request));
        apiResponse.setMessage("Dispute updated successfully");
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Dispute>> findAllDisputes(){
        ApiResponse<List<Dispute>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findAllDisputes());
        apiResponse.setMessage("Disputes fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{disputeId}")
    ApiResponse<DisputeResponse> getDisputeById(@PathVariable int disputeId) {
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findDisputeById(disputeId));
        apiResponse.setMessage("Dispute fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{disputeId}")
    ApiResponse<String> deleteDispute(@PathVariable int disputeId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.deleteDispute(disputeId));
        return apiResponse;
    }
}
