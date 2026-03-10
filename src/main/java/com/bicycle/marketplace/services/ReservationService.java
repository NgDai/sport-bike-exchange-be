package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.IBikeListingRepository;
import com.bicycle.marketplace.repository.IReservationRepository;
import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.ReservationMapper;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.services.Impl.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationMapper.updateReservation(reservation, request);
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservationStatus(int reservationId, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(request.getStatus());
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
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
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
        return "Reservation deleted successfully";
    }

    public List<Reservation> findReservationsByStatus(String status) {
        return reservationRepository.findAllByStatus(status);
    }
}
