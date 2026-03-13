package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.Dispute;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.IDisputeRepository;
import com.bicycle.marketplace.repository.IInspectionReportRepository;
import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.InspectionReport;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.InspectionReportMapper;
import com.bicycle.marketplace.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionReportService {
    @Autowired
    private IInspectionReportRepository inspectionReportRepository;
    @Autowired
    private InspectionReportMapper inspectionReportMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IDisputeRepository disputeRepository;

    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public InspectionReportResponse createInspectionReport(int disputeId, InspectionReportCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users inspector = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Dispute dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new AppException(ErrorCode.DISPUTE_NOT_FOUND));
        InspectionReport inspectionReport = inspectionReportMapper.toInspectionReport(request);
        inspectionReport.setInspector(inspector);
        inspectionReport.setDispute(dispute);
        return inspectionReportMapper.toInspectorReportResponse(inspectionReportRepository.save(inspectionReport));
    }

    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public InspectionReportResponse updateInspectionReport(int inspectionReportId,
            InspectionReportUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        InspectionReport inspectionReport = inspectionReportRepository.findById(inspectionReportId)
                .orElseThrow(() -> new AppException(ErrorCode.INSPECTIONREPORT_NOT_FOUND));
        inspectionReportMapper.updateInspectionReport(inspectionReport, request);
        return inspectionReportMapper.toInspectorReportResponse(inspectionReportRepository.save(inspectionReport));
    }

    public List<InspectionReport> findAllInspectionReports() {
        return inspectionReportRepository.findAll();
    }

    public InspectionReportResponse findInspectionReportById(int inspectionReportId) {
        InspectionReport inspectionReport = inspectionReportRepository.findById(inspectionReportId)
                .orElseThrow(() -> new AppException(ErrorCode.INSPECTIONREPORT_NOT_FOUND));
        return inspectionReportMapper.toInspectorReportResponse(inspectionReport);
    }

    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public String deleteInspectionReport(int inspectionReportId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        InspectionReport inspectionReport = inspectionReportRepository.findById(inspectionReportId)
                .orElseThrow(() -> new AppException(ErrorCode.INSPECTIONREPORT_NOT_FOUND));
        inspectionReportRepository.delete(inspectionReport);
        return "Inspection Report deleted successfully";
    }
}
