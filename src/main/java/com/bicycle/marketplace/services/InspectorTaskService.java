package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.response.InspectorTaskResponse;
import com.bicycle.marketplace.entities.Reservation;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.repository.IReservationRepository;
import com.bicycle.marketplace.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectorTaskService {

    private final IReservationRepository reservationRepository;
    private final IUserRepository userRepository;

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<InspectorTaskResponse> getMyTasks() {
        Users inspector = getCurrentUser();
        List<Reservation> tasks = reservationRepository.findByInspectorAndStatusIn(
                inspector,
                List.of("Scheduled", "Pending") // Chỉ lấy các nhiệm vụ đã được phân công
        );
        return tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InspectorTaskResponse getTaskById(int taskId) {
        Users inspector = getCurrentUser();
        Reservation reservation = reservationRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        if (reservation.getInspector() == null || reservation.getInspector().getUserId() != inspector.getUserId()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return mapToTaskResponse(reservation);
    }

    private InspectorTaskResponse mapToTaskResponse(Reservation reservation) {
        return InspectorTaskResponse.builder()
                .id(reservation.getReservationId())
                .bikeName(reservation.getListing().getTitle())
                .bikeImage(reservation.getListing().getImage_url())
                .price(reservation.getListing().getPrice())
                .buyerName(reservation.getBuyer().getFullName())
                .buyerPhone(reservation.getBuyer().getPhone())
                .sellerName(reservation.getListing().getSeller().getFullName())
                .sellerPhone(reservation.getListing().getSeller().getPhone())
                .location(reservation.getMeetingLocation())
                .scheduledTime(reservation.getMeetingTime())
                .status(reservation.getStatus())
                .build();
    }
}