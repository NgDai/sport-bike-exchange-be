package com.bicycle.marketplace.services;

import com.bicycle.marketplace.Repository.ICheckInRepository;
import com.bicycle.marketplace.dto.request.CheckInCreationRequest;
import com.bicycle.marketplace.dto.request.CheckInUpdateRequest;
import com.bicycle.marketplace.dto.response.CheckInResponse;
import com.bicycle.marketplace.entities.CheckIn;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.CheckInMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class CheckInService {
    @Autowired
    private ICheckInRepository checkInRepository;
    @Autowired
    private CheckInMapper checkInMapper;

    public CheckInResponse createCheckIn(CheckInCreationRequest request) {
        CheckIn checkIn = new CheckIn();

        checkIn.setRole(request.getRole().name());
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
}
