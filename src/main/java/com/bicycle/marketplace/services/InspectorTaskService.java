package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.response.InspectorTaskResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.EventBicycle;
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
                List.of("Scheduled", "Pending", "Waiting_Payment", "Inspection_Failed", "Completed", "Cancelled", "Refunded", "Compensated")
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
        InspectorTaskResponse.InspectorTaskResponseBuilder builder = InspectorTaskResponse.builder()
                .id(reservation.getReservationId())
                .buyerName(reservation.getBuyer().getFullName())
                .buyerPhone(reservation.getBuyer().getPhone())
                .buyerAvatar(reservation.getBuyer().getAvatar())
                .location(reservation.getMeetingLocation())
                .scheduledTime(reservation.getMeetingTime())
                .status(reservation.getStatus());

        if (reservation.getListing() != null) {
            BikeListing listing = reservation.getListing();
            builder.bikeName(listing.getTitle())
                    .bikeImage(listing.getImage_url())
                    .price(listing.getPrice())
                    .sellerName(listing.getSeller().getFullName())
                    .sellerPhone(listing.getSeller().getPhone())
                    .sellerAvatar(listing.getSeller().getAvatar())
                    .isEventBike(false);
        } else if (reservation.getEventBicycle() != null) {
            EventBicycle eventBike = reservation.getEventBicycle();
            builder.bikeName(eventBike.getTitle())
                    .bikeImage(eventBike.getImage_url())
                    .price(eventBike.getPrice())
                    .sellerName(eventBike.getSeller().getFullName())
                    .sellerPhone(eventBike.getSeller().getPhone())
                    .sellerAvatar(eventBike.getSeller().getAvatar())
                    .isEventBike(true);
        }

        return builder.build();
    }
}