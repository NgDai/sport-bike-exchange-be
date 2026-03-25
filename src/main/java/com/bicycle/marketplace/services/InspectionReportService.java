package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.InspectionReportCreationRequest;
import com.bicycle.marketplace.dto.request.InspectionReportUpdateRequest;
import com.bicycle.marketplace.dto.response.InspectionReportResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.InspectionReportMapper;
import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.repository.IInspectionReportRepository;
import com.bicycle.marketplace.repository.IReservationRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InspectionReportService {
    @Autowired
    private IInspectionReportRepository inspectionReportRepository;
    @Autowired
    private InspectionReportMapper inspectionReportMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private IReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService; // Used for refund in NO_SHOW


    private final ObjectMapper objectMapper = new ObjectMapper();

    private InspectionReportResponse toResponseEnriched(InspectionReport report) {
        InspectionReportResponse response = inspectionReportMapper.toInspectorReportResponse(report);
        if (report.getReservation() != null) {
            Reservation res = report.getReservation();
            if (res.getBuyer() != null) {
                response.setBuyerName(res.getBuyer().getFullName() != null ? res.getBuyer().getFullName() : res.getBuyer().getUsername());
            }
            if (res.getListing() != null && res.getListing().getSeller() != null) {
                Users seller = res.getListing().getSeller();
                response.setSellerName(seller.getFullName() != null ? seller.getFullName() : seller.getUsername());
            } else if (res.getEventBicycle() != null) {
                EventBicycle eb = res.getEventBicycle();
                if (eb.getSeller() != null) {
                    response.setSellerName(eb.getSeller().getFullName() != null ? eb.getSeller().getFullName() : eb.getSeller().getUsername());
                } else if (eb.getSellerName() != null) {
                    response.setSellerName(eb.getSellerName());
                }
            }
        }
        return response;
    }

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

        BikeListing listing = reservation.getListing();
        if (listing == null) {
            throw new RuntimeException("Giao dịch này không liên đới bài đăng nào!");
        }

        if (Boolean.TRUE.equals(request.getBuyerCheckin()) && Boolean.FALSE.equals(request.getSellerCheckin())) {
            request.setResult("SELLER_NO_SHOW");
            request.setReason(request.getReason() != null && !request.getReason().isEmpty() ? request.getReason() : "Giao dịch thất bại: Người bán không có mặt");
        } else if (Boolean.FALSE.equals(request.getBuyerCheckin()) && Boolean.TRUE.equals(request.getSellerCheckin())) {
            request.setResult("BUYER_NO_SHOW");
            request.setReason(request.getReason() != null && !request.getReason().isEmpty() ? request.getReason() : "Giao dịch thất bại: Người mua không có mặt");
        }

        InspectionReport inspectionReport = inspectionReportMapper.toInspectionReport(request);
        inspectionReport.setInspector(inspector);
        inspectionReport.setReservation(reservation);

        // Chuyển danh sách checklist sang JSON string
        if (request.getChecklistItems() != null && !request.getChecklistItems().isEmpty()) {
            try {
                String checklistJson = objectMapper.writeValueAsString(request.getChecklistItems());
                inspectionReport.setChecklistItems(checklistJson);
            } catch (Exception e) {
                log.warn("Lỗi khi convert checklist sang JSON: {}", e.getMessage());
            }
        }

        inspectionReport = inspectionReportRepository.save(inspectionReport);

        if ("SUCCESS".equalsIgnoreCase(request.getResult())) {
            reservation.setStatus("Waiting_Payment");
            listing.setStatus("Waiting_Payment");
            reservationRepository.save(reservation);
            bikeListingRepository.save(listing);
        } else if ("SELLER_NO_SHOW".equalsIgnoreCase(request.getResult())) {
            reservation.setStatus("Inspection_Failed"); // Trạng thái trung gian để refundDeposit được gọi
            reservation.setCancelDescription(request.getReason());
            listing.setStatus("Hidden");
            reservationRepository.save(reservation);
            bikeListingRepository.save(listing);
            // Người mua sẽ tự nhấn nút hoàn cọc trên UI
        } else if ("BUYER_NO_SHOW".equalsIgnoreCase(request.getResult())) {
            reservation.setStatus("Inspection_Failed");
            reservation.setCancelDescription(request.getReason());
            listing.setStatus("Available");
            reservationRepository.save(reservation);
            bikeListingRepository.save(listing);
            // Người bán sẽ tự nhấn nút nhận đền bù trên UI
        } else {
            reservation.setStatus("Inspection_Failed");
            reservation.setCancelDescription(request.getReason());
            listing.setStatus("Available");
            reservationRepository.save(reservation);
            bikeListingRepository.save(listing);
        }

        return toResponseEnriched(inspectionReport);
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

        // Cập nhật checklist nếu có gửi lên
        if (request.getChecklistItems() != null && !request.getChecklistItems().isEmpty()) {
            try {
                String checklistJson = objectMapper.writeValueAsString(request.getChecklistItems());
                inspectionReport.setChecklistItems(checklistJson);
            } catch (Exception e) {
                log.warn("Lỗi khi convert checklist sang JSON: {}", e.getMessage());
            }
        }

        return toResponseEnriched(inspectionReportRepository.save(inspectionReport));
    }

    public List<InspectionReport> findAllInspectionReports() {
        return inspectionReportRepository.findAll();
    }

    public InspectionReportResponse findInspectionReportById(int inspectionReportId) {
        InspectionReport inspectionReport = inspectionReportRepository.findById(inspectionReportId)
                .orElseThrow(() -> new AppException(ErrorCode.INSPECTIONREPORT_NOT_FOUND));
        return toResponseEnriched(inspectionReport);
    }

    // Cho phép buyer hoặc seller của reservation xem report (không cần role INSPECTOR)
    public InspectionReportResponse getReportByReservationId(int reservationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch này."));

        // Kiểm tra quyền: chỉ buyer, seller, inspector của reservation hoặc ADMIN mới được xem
        boolean isBuyer = reservation.getBuyer() != null
                && reservation.getBuyer().getUsername().equals(username);
        boolean isSeller = (reservation.getListing() != null
                && reservation.getListing().getSeller() != null
                && reservation.getListing().getSeller().getUsername().equals(username)) ||
                (reservation.getEventBicycle() != null
                && reservation.getEventBicycle().getSeller() != null
                && reservation.getEventBicycle().getSeller().getUsername().equals(username));
        boolean isInspector = reservation.getInspector() != null
                && reservation.getInspector().getUsername().equals(username);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isBuyer && !isSeller && !isInspector && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền xem báo cáo kiểm định này.");
        }

        InspectionReport report = inspectionReportRepository
                .findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Chưa có báo cáo kiểm định cho giao dịch này."));

        return toResponseEnriched(report);
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
