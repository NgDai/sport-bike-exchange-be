package com.bicycle.marketplace.services;

import com.bicycle.marketplace.repository.ICheckInRepository;
import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.entities.CheckIn;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.CheckInMapper;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public CheckInResponse createCheckIn(CheckInCreationRequest request) {
        CheckIn checkIn = new CheckIn();
        String token = UUID.randomUUID().toString();
        checkIn.setRole(request.getRole());
        checkIn.setToken(token);
        checkIn.setStatus(request.getStatus());
        checkIn.setCheckInTime(request.getCheckInTime());

        return checkInMapper.toCheckInResponse(checkInRepository.save(checkIn));
    }

    public CheckInResponse updateCheckIn(int checkInId, CheckInUpdateRequest request) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));
        checkInMapper.updateCheckIn(checkIn, request);
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
