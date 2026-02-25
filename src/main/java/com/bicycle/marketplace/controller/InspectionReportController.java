package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.InspectionReport;
import com.bicycle.marketplace.services.InspectionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inspection-reports")
public class InspectionReportController {
    @Autowired
    private InspectionReportService inspectionReportService;

    @PostMapping
    @PreAuthorize("hasRole('INSPECTOR') or hasRole('ADMIN')")
    ApiResponse<InspectionReportResponse> createInspectionReport(@RequestBody InspectionReportCreationRequest request) {
        ApiResponse<InspectionReportResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inspectionReportService.createInspectionReport(request));
        apiResponse.setMessage("Inspection Report created successfully");
        return apiResponse;
    }

    @PutMapping("/{reportId}")
    @PreAuthorize("hasRole('INSPECTOR') or hasRole('ADMIN')")
    ApiResponse<InspectionReportResponse> updateInspectionReport(@PathVariable int reportId,
            @RequestBody InspectionReportUpdateRequest request) {
        ApiResponse<InspectionReportResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inspectionReportService.updateInspectionReport(reportId, request));
        apiResponse.setMessage("Inspection Report updated successfully");
        return apiResponse;
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSPECTOR')")
    ApiResponse<InspectionReportResponse> getInspectionReportById(@PathVariable int reportId) {
        ApiResponse<InspectionReportResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inspectionReportService.findInspectionReportById(reportId));
        apiResponse.setMessage("Inspection Report fetched successfully");
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<java.util.List<InspectionReport>> getAllInspectionReports() {
        ApiResponse<java.util.List<InspectionReport>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inspectionReportService.findAllInspectionReports());
        apiResponse.setMessage("Inspection Reports fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteInspectionReport(@PathVariable int reportId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(inspectionReportService.deleteInspectionReport(reportId));
        return apiResponse;
    }
}
