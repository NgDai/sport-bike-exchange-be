package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.Reservation;
import com.bicycle.marketplace.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ApiResponse<ReservationResponse> createReservation(@RequestBody ReservationCreationRequest request) {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.createReservation(request));
        apiResponse.setMessage("Reservation created successfully");
        return apiResponse;
    }

    @PutMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ReservationResponse> updateReservation(@PathVariable int reservationId,
            @RequestBody ReservationUpdateRequest request) {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.updateReservation(reservationId, request));
        apiResponse.setMessage("Reservation updated successfully");
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<Reservation>> findAllReservations() {
        ApiResponse<List<Reservation>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.findAllReservations());
        apiResponse.setMessage("Reservations fetched successfully");
        return apiResponse;
    }

    @GetMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    ApiResponse<ReservationResponse> getReservationById(@PathVariable int reservationId) {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.findReservationById(reservationId));
        apiResponse.setMessage("Reservation fetched successfully");
        return apiResponse;
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteReservation(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.deleteReservation(reservationId));
        return apiResponse;
    }
}
