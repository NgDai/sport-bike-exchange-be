package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.IInspectionReportRepository;
import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.InspectionReport;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.InspectionReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionReportService {
    @Autowired
    private IInspectionReportRepository inspectionReportRepository;
    @Autowired
    private InspectionReportMapper inspectionReportMapper;

    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public InspectionReportResponse createInspectionReport(InspectionReportCreationRequest request) {
        InspectionReport inspectionReport = inspectionReportMapper.toInspectionReport(request);
        return inspectionReportMapper.toInspectorReportResponse(inspectionReportRepository.save(inspectionReport));
    }

    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    public InspectionReportResponse updateInspectionReport(int inspectionReportId,
            InspectionReportUpdateRequest request) {
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
        InspectionReport inspectionReport = inspectionReportRepository.findById(inspectionReportId)
                .orElseThrow(() -> new AppException(ErrorCode.INSPECTIONREPORT_NOT_FOUND));
        inspectionReportRepository.delete(inspectionReport);
        return "Inspection Report deleted successfully";
    }
}
