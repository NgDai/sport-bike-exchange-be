package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.services.DisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disputes")
public class DisputeController {
    @Autowired
    private DisputeService disputeService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ApiResponse<DisputeResponse> createDispute(@RequestBody DisputeCreationRequest request) {
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.createDispute(request));
        apiResponse.setMessage("Dispute created successfully");
        return apiResponse;
    }

    @PutMapping("/{disputeId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<DisputeResponse> updateDispute(@PathVariable int disputeId, @RequestBody DisputeUpdateRequest request) {
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.updateDispute(disputeId, request));
        apiResponse.setMessage("Dispute updated successfully");
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<Dispute>> findAllDisputes() {
        ApiResponse<List<Dispute>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findAllDisputes());
        apiResponse.setMessage("Disputes fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{disputeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    ApiResponse<DisputeResponse> getDisputeById(@PathVariable int disputeId) {
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findDisputeById(disputeId));
        apiResponse.setMessage("Dispute fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{disputeId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteDispute(@PathVariable int disputeId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.deleteDispute(disputeId));
        return apiResponse;
    }
}
