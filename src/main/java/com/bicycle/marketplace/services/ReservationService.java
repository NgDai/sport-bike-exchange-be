package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationScheduleRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.request.CancelReservationRequest;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Reservation;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.ReservationMapper;
import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.repository.IReservationRepository;
import com.bicycle.marketplace.repository.ITransactionRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private IReservationRepository reservationRepository;
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private ITransactionRepository transactionRepository;

    public ReservationResponse createReservation(int bikeListingId, ReservationCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BikeListing bikeListing = bikeListingRepository.findById(bikeListingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        Reservation reservation = new Reservation();
        reservation.setBuyer(buyer);
        reservation.setListing(bikeListing);
        reservation.setStatus("Reserved");
        reservation.setReservedAt(request.getReservedAt());
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservation(int reservationId, ReservationUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationMapper.updateReservation(reservation, request);
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservationStatus(int reservationId, ReservationUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(request.getStatus());
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    // --- HÀM MỚI: ADMIN LÊN LỊCH & GÁN INSPECTOR ---
    @Transactional
    public ReservationResponse scheduleReservation(int reservationId, ReservationScheduleRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 1. Lấy Reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. Lấy Inspector & Verify Role
        Users inspector = userRepository.findById(request.getInspectorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!"INSPECTOR".equalsIgnoreCase(inspector.getRole()) && !"ADMIN".equalsIgnoreCase(inspector.getRole())) {
            throw new RuntimeException("Người dùng được chọn không phải là Kiểm định viên hợp lệ!");
        }

        // 3. Gán dữ liệu
        reservation.setInspector(inspector);
        reservation.setMeetingLocation(request.getMeetingLocation());
        reservation.setMeetingTime(request.getMeetingTime());
        reservation.setLatitude(request.getLatitude());
        reservation.setLongitude(request.getLongitude());
        reservation.setStatus("Scheduled"); // Chuyển trạng thái thành đã lên lịch

        // 4. Đồng bộ trạng thái Transaction (Nếu có)
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            transaction.setStatus("Scheduled");
            transactionRepository.save(transaction);
        });

        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    public List<ReservationResponse> getMyReservations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Hàm này đã có sẵn trong IReservationRepository của bạn
        List<Reservation> reservations = reservationRepository
                .findByBuyer_UserIdAndStatusNotOrderByReservedAtDesc(user.getUserId(), "Cancelled");

        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAllByStatusNot("Cancelled");
    }

    public ReservationResponse findReservationById(int reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        return reservationMapper.toReservationResponse(reservation);
    }

    public String deleteReservation(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
        return "Reservation deleted successfully";
    }

    public List<Reservation> findReservationsByStatus(String status) {
        return reservationRepository.findAllByStatus(status);
    }

    public List<ReservationResponse> findAllReservationResponses() {
        return reservationRepository.findAllByStatusNot("Cancelled").stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    @Transactional
    public String cancelReservation(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getStatus().equalsIgnoreCase("Deposited")
                && !reservation.getStatus().equalsIgnoreCase("Reserved")
                && !reservation.getStatus().equalsIgnoreCase("Scheduled")) {
            throw new RuntimeException("Không thể hủy reservation ở trạng thái: " + reservation.getStatus());
        }

        BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        // 1. Xóa Transaction liên quan (nếu có) trước khi xóa Reservation
        transactionRepository.findByReservation_ReservationId(reservationId)
                .ifPresent(transactionRepository::delete);

        // 2. Xóa Reservation
        reservationRepository.delete(reservation);

        // 3. Đặt lại trạng thái listing về Available
        listing.setStatus("Available");
        bikeListingRepository.save(listing);

        return "Reservation và Transaction đã được xóa, xe đã trở về trạng thái Available.";
    }

    @Transactional
    public String requestCancelReservationBySeller(int reservationId, CancelReservationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getListing().getSeller().getUserId() != seller.getUserId()) {
            throw new RuntimeException("Chỉ người bán mới có quyền yêu cầu hủy giao dịch này.");
        }

        if (!reservation.getStatus().equalsIgnoreCase("Deposited")
                && !reservation.getStatus().equalsIgnoreCase("Reserved")
                && !reservation.getStatus().equalsIgnoreCase("Scheduled")) {
            throw new RuntimeException("Không thể yêu cầu hủy reservation ở trạng thái: " + reservation.getStatus());
        }

        reservation.setStatus("Pending_Cancel");
        reservation.setCancelDescription(request.getCancelDescription());
        reservationRepository.save(reservation);

        // Đồng bộ Transaction nếu có (chờ admin duyệt nên đổi status của transation cũng hợp lý, hoặc giữ nguyên nhưng đổi Reservation là đủ)
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            transaction.setStatus("Pending_Cancel");
            transactionRepository.save(transaction);
        });

        return "Yêu cầu hủy giao dịch đã được gửi và đang chờ Admin duyệt.";
    }

    @Transactional
    public String approveCancelReservationByAdmin(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getStatus().equalsIgnoreCase("Pending_Cancel")) {
            throw new RuntimeException("Reservation này không ở trạng thái chờ hủy.");
        }

        // Thực hiện hủy như cancel bình thường
        // Vì status hiện tại là Pending_Cancel nên ta gọi logic giống cancelReservation, nhưng bypass cái check status đó
        BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        transactionRepository.findByReservation_ReservationId(reservationId)
                .ifPresent(transactionRepository::delete);

        reservationRepository.delete(reservation);

        listing.setStatus("Available");
        bikeListingRepository.save(listing);

        return "Yêu cầu hủy giao dịch đã được duyệt. Đã hủy Reservation và Transaction.";
    }

    @Transactional
    public String rejectCancelReservationByAdmin(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getStatus().equalsIgnoreCase("Pending_Cancel")) {
            throw new RuntimeException("Reservation này không ở trạng thái chờ hủy.");
        }

        // Khôi phục lại trạng thái cũ
        // Nếu có inspector thì có thể là Scheduled, nếu không có inspector và có meeting info thì là Reserved, không तो là Deposited
        String restoredStatus = "Deposited"; 
        if (reservation.getInspector() != null && reservation.getMeetingTime() != null) {
            restoredStatus = "Scheduled";
        } else if (reservation.getMeetingTime() == null) {
            restoredStatus = "Deposited"; // Hoặc Reserved tuỳ luồng ban đầu
        } else {
             restoredStatus = "Reserved";
        }
        
        reservation.setStatus(restoredStatus);
        reservation.setCancelDescription(null);
        reservationRepository.save(reservation);

        final String finalRestoredStatus = restoredStatus;
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            transaction.setStatus(finalRestoredStatus);
            transactionRepository.save(transaction);
        });

        return "Yêu cầu hủy giao dịch đã bị từ chối. Trạng thái đã được khôi phục về " + restoredStatus + ".";
    }
}