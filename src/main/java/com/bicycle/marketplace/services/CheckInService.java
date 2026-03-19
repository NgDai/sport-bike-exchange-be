package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.repository.*;
import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.CheckInMapper;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class CheckInService {
    @Autowired
    private ICheckInRepository checkInRepository;
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;
    @Autowired
    private CheckInMapper checkInMapper;
    @Autowired
    private QRService qrService;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IEventRepository eventRepository;
    @Autowired
    private IReservationRepository reservationRepository;

    public CheckInResponse createCheckIn(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        CheckIn checkIn = new CheckIn();
        checkIn.setReservation(reservation);
        checkIn.setBuyer(reservation.getBuyer());
        checkIn.setBuyerName(reservation.getBuyer().getFullName());
        checkIn.setBuyerPhone(reservation.getBuyer().getPhone());
        if (reservation.getEventBicycle() != null) {
            checkIn.setEventBicycle(reservation.getEventBicycle());
            Users seller = reservation.getEventBicycle().getSeller();
            checkIn.setSeller(seller);
            checkIn.setSellerName(seller.getFullName());
            checkIn.setSellerPhone(seller.getPhone());
        } else if (reservation.getListing() != null) {
            checkIn.setEventBicycle(null);
            Users seller = reservation.getListing().getSeller();
            checkIn.setSeller(seller);
            checkIn.setSellerName(seller.getFullName());
            checkIn.setSellerPhone(seller.getPhone());
        }

        checkIn.setToken(token);
        return checkInMapper.toCheckInResponse(checkInRepository.save(checkIn));
    }

    public CheckInResponse updateCheckIn(int checkInId, CheckInUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        checkInMapper.updateCheckIn(checkIn, request);
        checkIn.setToken(UUID.randomUUID().toString());
        return checkInMapper.toCheckInResponse(checkInRepository.save(checkIn));
    }

    public List<CheckIn> findAllCheckIns() {
        return checkInRepository.findAll();
    }

    public CheckInResponse findCheckInById(int checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        return checkInMapper.toCheckInResponse(checkIn);
    }

    public String deleteCheckIn(int checkInId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        checkInRepository.delete(checkIn);
        return "Check-In deleted successfully";
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    public List<CheckIn> findCheckInsByStatus(String status) {
//        return checkInRepository.findByStatus(status);
//    }

    public byte[] createCheckInQRCode(int checkInId) throws IOException, WriterException {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        String token = checkIn.getToken();
        String url = "http://localhost:8080/qrcode?token=" + token;
        return qrService.generateQRCode(url);
    }

    public CheckIn getInfoFromQRCode(String token) {
        return checkInRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
    }
}
