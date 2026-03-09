package com.bicycle.marketplace.services;

import com.bicycle.marketplace.entities.Events;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.repository.ICheckInRepository;
import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.entities.CheckIn;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.CheckInMapper;
import com.bicycle.marketplace.repository.IEventRepository;
import com.bicycle.marketplace.repository.IUserRepository;
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
    private CheckInMapper checkInMapper;
    @Autowired
    private QRService qrService;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IEventRepository eventRepository;

    public CheckInResponse createCheckIn(int eventId, CheckInCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        CheckIn checkIn = new CheckIn();
        String token = UUID.randomUUID().toString();
        checkIn.setUser(user);
        checkIn.setEvent(event);
        checkIn.setToken(token);
        checkIn.setRole(request.getRole());
        checkIn.setStatus(request.getStatus());
        checkIn.setCheckInTime(request.getCheckInTime());

        return checkInMapper.toCheckInResponse(checkInRepository.save(checkIn));
    }

    public CheckInResponse updateCheckIn(int checkInId, CheckInUpdateRequest request) {
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
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        checkInRepository.delete(checkIn);
        return "Check-In deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<CheckIn> findCheckInsByStatus(String status) {
        return checkInRepository.findByStatus(status);
    }

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
