package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationScheduleRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
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
        Users buyer = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BikeListing bikeListing = bikeListingRepository.findById(bikeListingId).orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

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
        List<Reservation> reservations = reservationRepository.findByBuyer_UserIdOrderByReservedAtDesc(user.getUserId());

        return reservations.stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
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
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toReservationResponse)
                .toList();
    }
}