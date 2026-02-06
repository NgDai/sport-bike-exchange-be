package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.IReservationRepository;
import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private IReservationRepository reservationRepository;
    @Autowired
    private ReservationMapper reservationMapper;

    public ReservationResponse createReservation(ReservationCreationRequest request){
        Reservation reservation = new Reservation();
        reservation.setStatus(request.getStatus());
        reservation.setCreatedAt(request.getCreatedAt());
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservation(int reservationId, ReservationUpdateRequest request){
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationMapper.updateReservation(reservation, request);
        return reservationMapper.toReservationResponse(reservationRepository.save(reservation));
    }

    public List<Reservation> findAllReservations(){
        return reservationRepository.findAll();
    }

    public ReservationResponse findReservationById(int reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        return reservationMapper.toReservationResponse(reservation);
    }

    public String deleteReservation(int reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
        return "Reservation deleted successfully";
    }
}
