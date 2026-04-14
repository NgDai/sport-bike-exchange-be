package com.bicycle.marketplace.controller;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationScheduleRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.request.CancelReservationRequest;
import com.bicycle.marketplace.dto.response.ApiResponse;
import com.bicycle.marketplace.dto.response.CreateDepositResponse;
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

    @PostMapping("/{listingId}/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ApiResponse<ReservationResponse> createReservation(@PathVariable int listingId, @PathVariable int eventBikeId,
            @RequestBody ReservationCreationRequest request) {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.createReservation(listingId, eventBikeId, request));
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

    @PutMapping("/{reservationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ReservationResponse> updateReservationStatus(@PathVariable int reservationId,
            @RequestBody ReservationUpdateRequest request) {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.updateReservationStatus(reservationId, request));
        apiResponse.setMessage("Reservation status updated successfully");
        return apiResponse;
    }

    @GetMapping("/my-reservations")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ReservationResponse>> getMyReservations() {
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.getMyReservations());
        apiResponse.setMessage("Lấy danh sách đặt chỗ cá nhân thành công");
        return apiResponse;
    }

    @GetMapping("/eventBicycle/my-reservations")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ReservationResponse>> getMyEventBicycle() {
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.getMyReservationsWithEventBicycle());
        apiResponse.setMessage("Lấy danh sách đặt chỗ xe sự kiện thành công");
        return apiResponse;
    }

    @PutMapping("/{reservationId}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> scheduleReservation(
            @PathVariable int reservationId,
            @RequestBody ReservationScheduleRequest request) {
        ApiResponse<ReservationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.scheduleReservation(reservationId, request));
        apiResponse.setMessage("Scheduled reservation successfully");
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<ReservationResponse>> findAllReservations() {
        ApiResponse<List<ReservationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.findAllReservationResponses());
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

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<Reservation>> getReservationsByStatus(@PathVariable String status) {
        ApiResponse<List<Reservation>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.findReservationsByStatus(status));
        apiResponse.setMessage("Reservations fetched successfully");
        return apiResponse;
    }

    @PutMapping("/cancel/{reservationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    ApiResponse<String> cancelReservation(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.cancelReservation(reservationId));
        apiResponse.setMessage("Reservation cancelled successfully");
        return apiResponse;
    }

    @PutMapping("/{reservationId}/request-cancel")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<String> requestCancelReservation(
            @PathVariable int reservationId,
            @RequestBody CancelReservationRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.requestCancelReservation(reservationId, request));
        apiResponse.setMessage("Gửi yêu cầu hủy giao dịch thành công");
        return apiResponse;
    }

    @PutMapping("/{reservationId}/approve-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> approveCancelReservation(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.approveCancelReservationByAdmin(reservationId));
        apiResponse.setMessage("Duyệt yêu cầu hủy giao dịch thành công");
        return apiResponse;
    }

    @PutMapping("/{reservationId}/reject-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> rejectCancelReservation(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.rejectCancelReservationByAdmin(reservationId));
        apiResponse.setMessage("Từ chối yêu cầu hủy giao dịch thành công");
        return apiResponse;
    }

    @PostMapping("/{reservationId}/refund-inspection-fail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ApiResponse<String> refundDepositAfterInspectionFail(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.refundDepositAfterInspectionFail(reservationId));
        apiResponse.setMessage("Yêu cầu hoàn tiền cọc sau kiểm định thất bại đã được xử lý");
        return apiResponse;
    }

    @PostMapping("/{reservationId}/refund-inspection-fail-event")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ApiResponse<String> refundDepositAfterInspectionFailEvent(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.refundDepositAfterInspectionFailEvent(reservationId));
        apiResponse.setMessage("Yêu cầu hoàn tiền cọc sau kiểm định thất bại đã được xử lý");
        return apiResponse;
    }

    @PostMapping("/{reservationId}/compensation-buyer-no-show")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ApiResponse<String> transferDepositToSellerAfterBuyerNoShow(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.transferDepositToSellerAfterBuyerNoShow(reservationId));
        apiResponse.setMessage("Yêu cầu nhận tiền bồi thường đã được xử lý");
        return apiResponse;
    }

    @PostMapping("/{reservationId}/refund-success-transaction")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> refundDepositAfterTransaction(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.refundDepositAfterPaymentForEventBicycle(reservationId));
        apiResponse.setMessage("Hoàn tiền thành công!!");
        return apiResponse;
    }



    @PostMapping("/{reservationId}/final-payment")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<CreateDepositResponse> finalPayment(@PathVariable int reservationId) {
        ApiResponse<CreateDepositResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.finalPaymentForReservation(reservationId));
        apiResponse.setMessage("Xử lý thanh toán cuối giao dịch thành công");
        return apiResponse;
    }

    @PostMapping("/event-bicycle/{reservationId}/final-payment")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<CreateDepositResponse> finalPaymentEventBicycle(@PathVariable int reservationId) {
        ApiResponse<CreateDepositResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.finalPaymentForReservationEventBicycle(reservationId));
        apiResponse.setMessage("Thanh toán giao dịch thành công");
        return apiResponse;
    }

    @PostMapping("/event-bicycle/{reservationId}/confirm-offline-payment")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<String> confirmOfflinePayment(@PathVariable int reservationId) {
        reservationService.confirmPaymentOffline(reservationId);
        return new ApiResponse<>(200, "Success", "Giao dịch đã được xác nhận hoàn tất thành công.");
    }

    @PostMapping("/{reservationId}/payout-seller")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> payoutToSeller(@PathVariable int reservationId) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(reservationService.payoutToSeller(reservationId));
        apiResponse.setMessage("Chuyển tiền cho seller thành công");
        return apiResponse;
    }

}
