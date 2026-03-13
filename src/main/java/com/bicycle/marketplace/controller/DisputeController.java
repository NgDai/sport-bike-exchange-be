package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.DisputeCreationRequest;
import com.bicycle.marketplace.dto.request.DisputeUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.DisputeResponse;
import com.bicycle.marketplace.enums.DisputeStatus;
import com.bicycle.marketplace.services.DisputeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disputes")
public class DisputeController {
    @Autowired
    private DisputeService disputeService;

    @PostMapping("/transactions/{transactionId}/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ApiResponse<DisputeResponse> createDispute(@PathVariable int transactionId,
                                               @RequestBody @Valid DisputeCreationRequest request) {
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.createDispute(transactionId, request));
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
    ApiResponse<List<DisputeResponse>> findAllDisputes() {
        ApiResponse<List<DisputeResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findAllDisputes());
        apiResponse.setMessage("Disputes fetched successfully");
        return apiResponse;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ApiResponse<List<DisputeResponse>> findMyDisputes() {
        ApiResponse<List<DisputeResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findMyDisputes());
        apiResponse.setMessage("My disputes fetched successfully");
        return apiResponse;
    }

    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasRole('INSPECTOR') or hasRole('ADMIN')")
    ApiResponse<List<DisputeResponse>> findDisputesAssignedToMe() {
        ApiResponse<List<DisputeResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findDisputesAssignedToMe());
        apiResponse.setMessage("Disputes assigned to me fetched successfully");
        return apiResponse;
    }

    @PostMapping("/{disputeId}/assign-inspector/{inspectorId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<DisputeResponse> assignInspector(@PathVariable int disputeId, @PathVariable int inspectorId) {
        ApiResponse<DisputeResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.assignInspector(disputeId, inspectorId));
        apiResponse.setMessage("Inspector assigned successfully");
        return apiResponse;
    }

    @GetMapping("/{disputeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('INSPECTOR')")
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

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<DisputeResponse>> findDisputesByStatus(@PathVariable DisputeStatus status) {
        ApiResponse<List<DisputeResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(disputeService.findDisputesByStatus(status));
        apiResponse.setMessage("Disputes fetched successfully");
        return apiResponse;
    }
}
