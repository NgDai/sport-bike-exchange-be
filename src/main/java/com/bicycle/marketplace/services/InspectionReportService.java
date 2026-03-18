package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.IReservationRepository;
import com.bicycle.marketplace.repository.IInspectionReportRepository;
import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.InspectionReport;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.InspectionReportMapper;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.entities.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
    private IReservationRepository reservationRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;

    @PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")
    @Transactional
    public InspectionReportResponse createInspectionReportForReservation(int reservationId, InspectionReportCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users inspector = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getInspector() == null || reservation.getInspector().getUserId() != inspector.getUserId()) {
            throw new RuntimeException("Bạn không phải là Kiểm định viên được phân công cho giao dịch này!");
        }

        InspectionReport inspectionReport = inspectionReportMapper.toInspectionReport(request);
        inspectionReport.setInspector(inspector);
        inspectionReport.setReservation(reservation);
        inspectionReport = inspectionReportRepository.save(inspectionReport);

        BikeListing listing = reservation.getListing();
        if (listing == null) {
            throw new RuntimeException("Giao dịch này không liên đới bài đăng nào!");
        }

        if ("SUCCESS".equalsIgnoreCase(request.getResult())) {
            reservation.setStatus("Waiting_Payment");
            listing.setStatus("Waiting_Payment");
        } else {
            reservation.setStatus("Inspection_Failed");
            listing.setStatus("Available");
        }

        reservationRepository.save(reservation);
        bikeListingRepository.save(listing);

        return inspectionReportMapper.toInspectorReportResponse(inspectionReport);
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
