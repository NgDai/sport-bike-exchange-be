package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.ReservationCreationRequest;
import com.bicycle.marketplace.dto.request.ReservationScheduleRequest;
import com.bicycle.marketplace.dto.request.ReservationUpdateRequest;
import com.bicycle.marketplace.dto.request.CancelReservationRequest;
import com.bicycle.marketplace.dto.response.CreateDepositResponse;
import com.bicycle.marketplace.dto.response.ReservationResponse;
import com.bicycle.marketplace.entities.BikeListing;
import com.bicycle.marketplace.entities.EventBicycle;
import com.bicycle.marketplace.entities.Reservation;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.entities.Wallet;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.ReservationMapper;
import com.bicycle.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ITransactionRepository transactionRepository;
    @Autowired
    private IDepositRepository depositRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;
    @Autowired
    private IWalletRepository walletRepository;
    @Autowired
    private WalletTransactionService walletTransactionService;
    @Autowired
    private VNPayService vnPayService;

    @Value("${vnpay.returnUrl}")
    private String vnpayReturnUrl;

    public ReservationResponse createReservation(int bikeListingId, int eventBikeId,
            ReservationCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BikeListing bikeListing = bikeListingRepository.findById(bikeListingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        Reservation reservation = new Reservation();
        reservation.setBuyer(buyer);
        reservation.setListing(bikeListing);
        reservation.setStatus("Reserved");
        reservation.setEventBicycle(eventBicycle);
        reservation.setReservedAt(request.getReservedAt());
        return toReservationResponseSafe(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservation(int reservationId, ReservationUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationMapper.updateReservation(reservation, request);
        return toReservationResponseSafe(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservationStatus(int reservationId, ReservationUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(request.getStatus());
        return toReservationResponseSafe(reservationRepository.save(reservation));
    }

    // --- HÀM MỚI: ADMIN LÊN LỊCH & GÁN INSPECTOR ---
    @Transactional
    public ReservationResponse scheduleReservation(int reservationId, ReservationScheduleRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 1. Lấy Reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. Lấy Inspector & Verify Role
        Users inspector = userRepository.findById(request.getInspectorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!"INSPECTOR".equalsIgnoreCase(inspector.getRole()) && !"ADMIN".equalsIgnoreCase(inspector.getRole())) {
            throw new RuntimeException("Người dùng được chọn không phải là Kiểm định viên hợp lệ!");
        }

        // 3. Gán dữ liệu
        reservation.setInspector(inspector);
        reservation.setMeetingLocation(request.getMeetingLocation());
        reservation.setMeetingTime(request.getMeetingTime());
        reservation.setLatitude(request.getLatitude());
        reservation.setLongitude(request.getLongitude());
        reservation.setStatus("Scheduled"); // Chuyển trạng thái thành đã lên lịch

        // 4. Đồng bộ trạng thái Transaction (Nếu có)
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            transaction.setStatus("Scheduled");
            transactionRepository.save(transaction);
        });

        return toReservationResponseSafe(reservationRepository.save(reservation));
    }

    public List<ReservationResponse> getMyReservations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Hàm này đã có sẵn trong IReservationRepository của bạn
        List<Reservation> reservations = reservationRepository
                .findByBuyer_UserIdAndStatusNotOrderByReservedAtDesc(user.getUserId(), "Cancelled");

        return reservations.stream()
                .map(this::toReservationResponseSafe)
                .toList();
    }

    public List<ReservationResponse> getMyReservationsWithEventBicycle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Reservation> reservations = reservationRepository
                .findByBuyer_UserIdAndEventBicycleNotNull(user.getUserId());
        return reservations.stream()
                .map(this::toReservationResponseSafe)
                .toList();
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAllByStatusNot("Cancelled");
    }

    public ReservationResponse findReservationById(int reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        return toReservationResponseSafe(reservation);
    }

    public String deleteReservation(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
        return "Reservation deleted successfully";
    }

    public List<Reservation> findReservationsByStatus(String status) {
        return reservationRepository.findAllByStatus(status);
    }

    public List<ReservationResponse> findAllReservationResponses() {
        return reservationRepository.findAllByStatusNot("Cancelled").stream()
                .map(this::toReservationResponseSafe)
                .toList();
    }

    @Transactional
    public String cancelReservation(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        boolean isWaitingPaymentDeposit = reservation.getStatus().equalsIgnoreCase("Waiting_Payment")
                && reservation.getDeposit() != null
                && "Waiting_Payment".equalsIgnoreCase(reservation.getDeposit().getStatus());

        if (!reservation.getStatus().equalsIgnoreCase("Deposited")
                && !reservation.getStatus().equalsIgnoreCase("Reserved")
                && !reservation.getStatus().equalsIgnoreCase("Scheduled")
                && !isWaitingPaymentDeposit) {
            throw new RuntimeException("Không thể hủy reservation ở trạng thái: " + reservation.getStatus());
        }

        // 1. Cập nhật Transaction liên quan (nếu có) và xóa Deposit trước khi xóa
        // Reservation
        transactionRepository.findByReservation_ReservationId(reservationId)
                .ifPresent(transaction -> {
                    // Lấy Deposit từ Transaction rồi xóa
                    var deposit = transaction.getDeposit();

                    // Giữ lại transaction làm lịch sử, cập nhật status và xóa reference
                    transaction.setStatus("Cancelled");
                    transaction.setReservation(null);
                    transaction.setDeposit(null);
                    transactionRepository.save(transaction);

                    if (deposit != null) {
                        depositRepository.delete(deposit);
                    }
                });

        // 2. Xóa Reservation
        reservationRepository.delete(reservation);

        // 3. Đặt lại trạng thái listing về Available
        if (reservation.getListing() != null) {
            BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
            listing.setStatus("Available");
            bikeListingRepository.save(listing);
        }

        if (reservation.getEventBicycle() != null) {
            EventBicycle eventBicycle = eventBicycleRepository.findById(reservation.getEventBicycle().getEventBikeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
            eventBicycle.setStatus("Available_in_event");
            eventBicycleRepository.save(eventBicycle);
        }

        return "Reservation và Deposit đã được xóa, Transaction được giữ lại làm lịch sử, xe đã trở về trạng thái Available.";
    }

    @Transactional
    public String requestCancelReservationBySeller(int reservationId, CancelReservationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        Integer sellerId = null;
        if (reservation.getListing() != null) {
            sellerId = reservation.getListing().getSeller().getUserId();
        } else if (reservation.getEventBicycle() != null && reservation.getEventBicycle().getSeller() != null) {
            sellerId = reservation.getEventBicycle().getSeller().getUserId();
        }

        if (sellerId == null || sellerId != seller.getUserId()) {
            throw new RuntimeException("Chỉ người bán mới có quyền yêu cầu hủy giao dịch này.");
        }

        boolean isWaitingPaymentDeposit = reservation.getStatus().equalsIgnoreCase("Waiting_Payment")
                && reservation.getDeposit() != null
                && "Waiting_Payment".equalsIgnoreCase(reservation.getDeposit().getStatus());

        if (!reservation.getStatus().equalsIgnoreCase("Deposited")
                && !reservation.getStatus().equalsIgnoreCase("Reserved")
                && !reservation.getStatus().equalsIgnoreCase("Scheduled")
                && !isWaitingPaymentDeposit) {
            throw new RuntimeException("Không thể yêu cầu hủy reservation ở trạng thái: " + reservation.getStatus());
        }

        reservation.setStatus("Pending_Cancel");
        reservation.setCancelDescription(request.getCancelDescription());
        reservation.setCancelImage(request.getCancelImage());
        reservationRepository.save(reservation);

        // Đồng bộ Transaction nếu có (chờ admin duyệt nên đổi status của transation
        // cũng hợp lý, hoặc giữ nguyên nhưng đổi Reservation là đủ)
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            transaction.setStatus("Pending_Cancel");
            transactionRepository.save(transaction);
        });

        return "Yêu cầu hủy giao dịch đã được gửi và đang chờ Admin duyệt.";
    }

    @Transactional
    public String approveCancelReservationByAdmin(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getStatus().equalsIgnoreCase("Pending_Cancel")) {
            throw new RuntimeException("Reservation này không ở trạng thái chờ hủy.");
        }

        // Thực hiện hủy như cancel bình thường
        // Vì status hiện tại là Pending_Cancel nên ta gọi logic giống
        // cancelReservation, nhưng bypass cái check status đó
        transactionRepository.findByReservation_ReservationId(reservationId)
                .ifPresent(transaction -> {
                    var deposit = transaction.getDeposit();

                    // Giữ lại transaction làm lịch sử, cập nhật status và xóa reference
                    transaction.setStatus("Cancelled");
                    transaction.setReservation(null);
                    transaction.setDeposit(null);
                    transactionRepository.save(transaction);

                    if (deposit != null) {
                        depositRepository.delete(deposit);
                    }
                });

        reservationRepository.delete(reservation);

        if (reservation.getListing() != null) {
            BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
            listing.setStatus("Available");
            bikeListingRepository.save(listing);
        }

        if (reservation.getEventBicycle() != null) {
            EventBicycle eventBicycle = eventBicycleRepository.findById(reservation.getEventBicycle().getEventBikeId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));
            eventBicycle.setStatus("Available_in_event");
            eventBicycleRepository.save(eventBicycle);
        }

        return "Yêu cầu hủy giao dịch đã được duyệt. Đã hủy Reservation và Deposit, Transaction được giữ lại làm lịch sử.";
    }

    @Transactional
    public String rejectCancelReservationByAdmin(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getStatus().equalsIgnoreCase("Pending_Cancel")) {
            throw new RuntimeException("Reservation này không ở trạng thái chờ hủy.");
        }

        // Khôi phục lại trạng thái cũ
        // Nếu có inspector thì có thể là Scheduled, nếu không có inspector và có
        // meeting info thì là Reserved, không तो là Deposited
        String restoredStatus = "Deposited";
        if (reservation.getInspector() != null && reservation.getMeetingTime() != null) {
            restoredStatus = "Scheduled";
        } else if (reservation.getMeetingTime() == null) {
            restoredStatus = "Deposited"; // Hoặc Reserved tuỳ luồng ban đầu
        } else {
            restoredStatus = "Reserved";
        }

        reservation.setStatus(restoredStatus);
        reservation.setCancelDescription(null);
        reservation.setCancelImage(null);
        reservationRepository.save(reservation);

        final String finalRestoredStatus = restoredStatus;
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            transaction.setStatus(finalRestoredStatus);
            transactionRepository.save(transaction);
        });

        return "Yêu cầu hủy giao dịch đã bị từ chối. Trạng thái đã được khôi phục về " + restoredStatus + ".";
    }

    @Transactional
    public String refundDepositAfterInspectionFail(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Inspection_Failed".equalsIgnoreCase(reservation.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể hoàn tiền cho giao dịch có trạng thái Inspection_Failed. Trạng thái hiện tại: "
                            + reservation.getStatus());
        }

        // Kiểm tra quyền: chỉ buyer, admin hoặc inspector mới được thực hiện
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isInspector = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSPECTOR"));
        boolean isBuyer = reservation.getBuyer() != null
                && reservation.getBuyer().getUsername().equals(username);
        if (!isAdmin && !isBuyer && !isInspector) {
            throw new RuntimeException("Bạn không có quyền thực hiện hoàn tiền cho giao dịch này.");
        }

        // Lấy số tiền cọc
        Double amount = reservation.getDepositAmount();
        if (amount == null && reservation.getDeposit() != null) {
            amount = reservation.getDeposit().getAmount();
        }
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Không tìm thấy số tiền cọc hợp lệ hoặc tiền cọc bằng 0");
        }

        // Hoàn tiền cọc 100% từ System Wallet về ví buyer
        walletService.refundToUserWallet(amount, reservation.getBuyer().getUsername(),
                "Hoàn tiền cọc do kiểm định thất bại - Giao dịch #" + reservationId);

        // Nếu là SELLER_NO_SHOW → thưởng thêm 200,000 VND cho buyer
        boolean isSellerNoShow = reservation.getCancelDescription() != null
                && reservation.getCancelDescription().toLowerCase().contains("người bán không có mặt");
        if (isSellerNoShow) {
            double bonus = 200000;
            walletService.refundToUserWallet(bonus, reservation.getBuyer().getUsername(),
                    "Tiền bồi thường thêm 200,000 VND do người bán không đến - Giao dịch #" + reservationId);
        }

        // Cập nhật Transaction: tách reference, đặt status Refunded
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            var deposit = transaction.getDeposit();
            transaction.setStatus("Refunded");
            transaction.setDeposit(null);
            transactionRepository.save(transaction);
            // Xóa Deposit sau khi tách khỏi Transaction
            if (deposit != null) {
                depositRepository.delete(deposit);
            }
        });

        // Nếu reservation vẫn có Deposit nhưng Transaction không track, xóa luôn
        if (reservation.getDeposit() != null) {
            var deposit = reservation.getDeposit();
            reservation.setDeposit(null);
            reservationRepository.save(reservation);
            depositRepository.delete(deposit);
        }

        // Đảm bảo listing về Available (InspectionReportService đã làm, đây là safety
        // net)
        BikeListing listing = reservation.getListing();
        if (listing != null && !"Available".equalsIgnoreCase(listing.getStatus())) {
            listing.setStatus("Available");
            bikeListingRepository.save(listing);
        }

        // Đặt reservation thành Refunded (đã hoàn tiền xong)
        reservation.setStatus("Refunded");
        reservationRepository.save(reservation);

        String message = "Đã hoàn tiền cọc " + amount.longValue() + " VND cho người mua.";
        if (isSellerNoShow) {
            message += " Đã cộng thêm 200,000 VND tiền bồi thường.";
        }
        return message;
    }

    @Transactional
    public String refundDepositAfterInspectionFailEvent(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Inspection_Failed".equalsIgnoreCase(reservation.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể hoàn tiền cho giao dịch có trạng thái Inspection_Failed. Trạng thái hiện tại: "
                            + reservation.getStatus());
        }

        // Kiểm tra quyền: chỉ buyer, admin hoặc inspector mới được thực hiện
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isInspector = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INSPECTOR"));
        boolean isBuyer = reservation.getBuyer() != null
                && reservation.getBuyer().getUsername().equals(username);
        if (!isAdmin && !isBuyer && !isInspector) {
            throw new RuntimeException("Bạn không có quyền thực hiện hoàn tiền cho giao dịch này.");
        }

        // Lấy số tiền cọc
        Double amount = reservation.getDepositAmount();
        if (amount == null && reservation.getDeposit() != null) {
            amount = reservation.getDeposit().getAmount();
        }
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Không tìm thấy số tiền cọc hợp lệ hoặc tiền cọc bằng 0");
        }

        // Hoàn tiền cọc 100% từ System Wallet về ví buyer
        walletService.refundToUserWallet(amount, reservation.getBuyer().getUsername(),
                "Hoàn tiền cọc do kiểm định thất bại - Giao dịch #" + reservationId);

        // Nếu là SELLER_NO_SHOW → thưởng thêm 200,000 VND cho buyer
        boolean isSellerNoShow = reservation.getCancelDescription() != null
                && reservation.getCancelDescription().toLowerCase().contains("người bán không có mặt");
        if (isSellerNoShow) {
            double bonus = 200000;
            walletService.refundToUserWallet(bonus, reservation.getBuyer().getUsername(),
                    "Tiền bồi thường thêm 200,000 VND do người bán không đến - Giao dịch #" + reservationId);
        }

        // Cập nhật Transaction: tách reference, đặt status Refunded
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            var deposit = transaction.getDeposit();
            transaction.setStatus("Refunded");
            transaction.setDeposit(null);
            transactionRepository.save(transaction);
            // Xóa Deposit sau khi tách khỏi Transaction
            if (deposit != null) {
                depositRepository.delete(deposit);
            }
        });

        // Nếu reservation vẫn có Deposit nhưng Transaction không track, xóa luôn
        if (reservation.getDeposit() != null) {
            var deposit = reservation.getDeposit();
            reservation.setDeposit(null);
            reservationRepository.save(reservation);
            depositRepository.delete(deposit);
        }

        // Đảm bảo event về Available (InspectionReportService đã làm, đây là safety
        // net)
        EventBicycle event = reservation.getEventBicycle();
        if (event != null && !"Available".equalsIgnoreCase(event.getStatus())) {
            event.setStatus("Available_in_event");
            eventBicycleRepository.save(event);
        }

        // Đặt reservation thành Refunded (đã hoàn tiền xong)
        reservation.setStatus("Refunded");
        reservationRepository.save(reservation);

        String message = "Đã hoàn tiền cọc " + amount.longValue() + " VND cho người mua.";
        if (isSellerNoShow) {
            message += " Đã cộng thêm 200,000 VND tiền bồi thường.";
        }
        return message;
    }

    @Transactional
    public String refundDepositAfterPaymentForEventBicycle(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        // Kiểm tra quyền: chỉ buyer hoặc admin mới được thực hiện
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new RuntimeException("Bạn không có quyền thực hiện hoàn tiền cho giao dịch này.");
        }

        // Lấy số tiền cọc
        Double amount = reservation.getDepositAmount();
        if (amount == null && reservation.getDeposit() != null) {
            amount = reservation.getDeposit().getAmount();
        }
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Không tìm thấy số tiền cọc hợp lệ hoặc tiền cọc bằng 0");
        }

        // Hoàn tiền từ System Wallet về ví buyer
        walletService.refundToUserWallet(amount, reservation.getBuyer().getUsername(),
                "Hoàn tiền cọc cho Giao dịch #" + reservationId);

        // Cập nhật Transaction: tách reference, đặt status Refunded
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            var deposit = transaction.getDeposit();
            transaction.setStatus("Refunded");
            transaction.setReservation(null);
            transaction.setDeposit(null);
            transactionRepository.save(transaction);
            // Xóa Deposit sau khi tách khỏi Transaction
            if (deposit != null) {
                depositRepository.delete(deposit);
            }
        });

        // Nếu reservation vẫn có Deposit nhưng Transaction không track, xóa luôn
        if (reservation.getDeposit() != null) {
            var deposit = reservation.getDeposit();
            reservation.setDeposit(null);
            reservationRepository.save(reservation);
            depositRepository.delete(deposit);
        }

        EventBicycle eventBicycle = reservation.getEventBicycle();
        if (eventBicycle != null) {
            eventBicycle.setStatus("Sold");
            eventBicycleRepository.save(eventBicycle);
        }

        // Đặt reservation thành Cancelled (đã hoàn tiền xong)
        reservation.setStatus("Completed");
        reservationRepository.save(reservation);

        return "Đã hoàn tiền cọc " + amount.longValue() + " VND thành công cho người mua.";
    }

    @Transactional
    public String transferDepositToSellerAfterBuyerNoShow(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Inspection_Failed".equalsIgnoreCase(reservation.getStatus())
                && !"Cancelled".equalsIgnoreCase(reservation.getStatus())) {
            // Since BUYER_NO_SHOW sets status to Cancelled or Inspection_Failed. We will
            // just check if we can process it.
            // Normally the caller handles the status.
        }

        // Lấy số tiền cọc
        Double amount = reservation.getDepositAmount();
        if (amount == null && reservation.getDeposit() != null) {
            amount = reservation.getDeposit().getAmount();
        }
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Không tìm thấy số tiền cọc hợp lệ hoặc tiền cọc bằng 0");
        }

        // Xác định Seller: ưu tiên từ listing, sau đó từ eventBicycle
        Users seller = null;
        if (reservation.getListing() != null) {
            BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
            seller = listing.getSeller();
        } else if (reservation.getEventBicycle() != null) {
            EventBicycle eb = reservation.getEventBicycle();
            seller = eb.getSeller();
        }

        if (seller == null) {
            throw new RuntimeException("Không tìm thấy thông tin seller để chuyển tiền.");
        }

        // Chia tiền cọc: Seller nhận 50%, System giữ 50%
        double sellerShare = amount * 0.5;

        walletService.refundToUserWallet(sellerShare, seller.getUsername(),
                "Nhận 50% tiền cọc (đền bù do người mua không đến kiểm định) - Giao dịch #" + reservationId);

        // Cập nhật Transaction: tách reference, đặt status Compensated
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(transaction -> {
            var deposit = transaction.getDeposit();
            transaction.setStatus("Compensated");
            transaction.setDeposit(null);
            transactionRepository.save(transaction);
            // Xóa Deposit sau khi tách khỏi Transaction
            if (deposit != null) {
                depositRepository.delete(deposit);
            }
        });

        // Nếu reservation vẫn có Deposit nhưng Transaction không track, xóa luôn
        if (reservation.getDeposit() != null) {
            var deposit = reservation.getDeposit();
            reservation.setDeposit(null);
            reservationRepository.save(reservation);
            depositRepository.delete(deposit);
        }

        // Đảm bảo listing về Available
        BikeListing listing = reservation.getListing();
        if (listing != null && !"Available".equalsIgnoreCase(listing.getStatus())) {
            listing.setStatus("Available");
            bikeListingRepository.save(listing);
        }

        // Đặt reservation thành Compensated
        reservation.setStatus("Compensated");
        reservationRepository.save(reservation);

        return "Đã chuyển 50% tiền cọc (" + (long) sellerShare + " VND) cho người bán. Hệ thống giữ lại 50% còn lại. Giao dịch đã được đền bù.";
    }

    // ==========================================
    // THANH TOÁN CUỐI SAU KHI KIỂM ĐỊNH THÀNH CÔNG
    // ==========================================

    @Transactional
    public CreateDepositResponse finalPaymentForReservation(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Waiting_Payment".equalsIgnoreCase(reservation.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể thanh toán cuối cho giao dịch đã hoàn thành kiểm định (Waiting_Payment). Trạng thái hiện tại: "
                            + reservation.getStatus());
        }

        if (reservation.getBuyer() == null || reservation.getBuyer().getUserId() != buyer.getUserId()) {
            throw new RuntimeException("Bạn không phải người mua của giao dịch này.");
        }

        BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        double listingPrice = listing.getPrice();
        double depositAmount = reservation.getDepositAmount() != null ? reservation.getDepositAmount() : 0;
        double remainingAmount = listingPrice - depositAmount;

        if (remainingAmount <= 0) {
            throw new RuntimeException(
                    "Số tiền còn lại không hợp lệ (" + remainingAmount + "). Giao dịch có thể đã thanh toán đủ.");
        }

        Wallet buyerWallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder().user(buyer).username(username).balance(0.0).type("User").build();
            return walletRepository.save(newWallet);
        });

        // TRƯỜNG HỢP 1: VÍ ĐỦ TIỀN → TRỪ THẲNG
        if (buyerWallet.getBalance() >= remainingAmount) {
            completeReservation(reservation, listing, remainingAmount, username);
            return CreateDepositResponse.builder()
                    .deposit(null)
                    .paymentUrl(null)
                    .message("Thanh toán thành công! Đã trừ " + (long) remainingAmount
                            + " VND từ ví. Giao dịch hoàn tất.")
                    .build();
        }

        // TRƯỜNG HỢP 2: VÍ KHÔNG ĐỦ → TẠO VNPAY URL
        long amountNeeded = (long) Math.ceil(remainingAmount - buyerWallet.getBalance());
        String customReturnUrl = vnpayReturnUrl + "?reservationId=" + reservationId;
        String paymentUrl = vnPayService.createOrder(
                amountNeeded,
                username + "|finalpayment|" + reservationId,
                customReturnUrl,
                null);

        return CreateDepositResponse.builder()
                .deposit(null)
                .paymentUrl(paymentUrl)
                .message("Ví không đủ tiền. Vui lòng thanh toán thêm " + amountNeeded
                        + " VND qua VNPay để hoàn tất giao dịch.")
                .build();
    }

    @Transactional
    public void confirmFinalPayment(int reservationId, String username, double vnpayAmount) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Waiting_Payment".equalsIgnoreCase(reservation.getStatus())) {
            // Đã được xử lý rồi (idempotent)
            return;
        }

        BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        double listingPrice = listing.getPrice();
        double depositAmount = reservation.getDepositAmount() != null ? reservation.getDepositAmount() : 0;
        double remainingAmount = listingPrice - depositAmount;

        // Nạp tiền từ VNPay vào ví Buyer
        Wallet buyerWallet = walletRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        buyerWallet.setBalance(buyerWallet.getBalance() + vnpayAmount);
        walletRepository.save(buyerWallet);
        walletTransactionService.createTransaction(buyerWallet, vnpayAmount, "FinalPayment_TopUp",
                "Nạp tiền qua VNPay để thanh toán cuối giao dịch #" + reservationId);

        // Hoàn tất giao dịch (trừ phần còn lại ra khỏi ví)
        completeReservation(reservation, listing, remainingAmount, username);
    }

    @Transactional
    public String payoutToSeller(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Completed".equalsIgnoreCase(reservation.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể chuyển tiền cho seller khi giao dịch đã hoàn thành (Completed). "
                            + "Trạng thái hiện tại: " + reservation.getStatus());
        }

        // Kiểm tra đã payout chưa
        var txnOpt = transactionRepository.findByReservation_ReservationId(reservationId);
        if (txnOpt.isPresent() && "Paid_Out".equalsIgnoreCase(txnOpt.get().getStatus())) {
            if (!"Paid_Out".equalsIgnoreCase(reservation.getStatus())) {
                reservation.setStatus("Paid_Out");
                reservationRepository.save(reservation);
            }
            return "Trạng thái đã được đồng bộ: Seller đã nhận được tiền trước đó.";
        }

        // Xác định Seller: ưu tiên từ listing, sau đó từ eventBicycle
        Users seller = null;
        double totalAmount = 0;

        if (reservation.getListing() != null) {
            BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
            seller = listing.getSeller();
            totalAmount = listing.getPrice();
        } else if (reservation.getEventBicycle() != null) {
            EventBicycle eb = reservation.getEventBicycle();
            seller = eb.getSeller();
            totalAmount = eb.getPrice() != null ? eb.getPrice() : 0;
        }

        if (seller == null) {
            throw new RuntimeException("Không tìm thấy thông tin seller để chuyển tiền.");
        }
        if (totalAmount <= 0) {
            throw new RuntimeException("Số tiền chuyển cho seller không hợp lệ: " + totalAmount);
        }

        // Chuyển tiền từ System Wallet → Seller Wallet (dùng hàm refundToUserWallet)
        walletService.refundToUserWallet(
                totalAmount,
                seller.getUsername(),
                "Thanh toán từ hệ thống cho người bán - Giao dịch đặt chỗ #" + reservationId);

        // Cập nhật Transaction status → Paid_Out
        transactionRepository.findByReservation_ReservationId(reservationId).ifPresent(txn -> {
            txn.setStatus("Paid_Out");
            transactionRepository.save(txn);
        });

        reservation.setStatus("Paid_Out");
        reservationRepository.save(reservation);

        return "Đã chuyển " + (long) totalAmount + " VND từ ví hệ thống sang ví seller ("
                + seller.getUsername() + ") thành công.";
    }

    public void cancelFinalPayment(int reservationId) {
        // Không làm gì cả — reservation vẫn giữ nguyên status Waiting_Payment
        // để người dùng có thể thanh toán lại sau.
        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            if (!"Waiting_Payment".equalsIgnoreCase(reservation.getStatus())) {
                return; // Đã hoàn tất hoặc đã bị hủy bởi luồng khác
            }
            // Giữ nguyên status, không cần làm gì thêm
        });
    }

    private void completeReservation(Reservation reservation, BikeListing listing, double remainingAmount,
            String buyerUsername) {
        Wallet buyerWallet = walletRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình System Wallet"));

        if (buyerWallet.getBalance() < remainingAmount) {
            throw new RuntimeException(
                    "Ví không đủ tiền để hoàn tất thanh toán. Số dư hiện tại: " + (long) buyerWallet.getBalance()
                            + " VND, cần thêm: " + (long) (remainingAmount - buyerWallet.getBalance()) + " VND.");
        }

        // Chuyển tiền Buyer → System Wallet
        buyerWallet.setBalance(buyerWallet.getBalance() - remainingAmount);
        systemWallet.setBalance(systemWallet.getBalance() + remainingAmount);
        walletRepository.save(buyerWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(buyerWallet, remainingAmount, "FinalPayment",
                "Thanh toán cuối giao dịch xe đạp #" + listing.getListingId());

        // Cập nhật trạng thái
        reservation.setStatus("Completed");
        reservationRepository.save(reservation);

        listing.setStatus("Sold");
        bikeListingRepository.save(listing);

        transactionRepository.findByReservation_ReservationId(reservation.getReservationId()).ifPresent(txn -> {
            txn.setStatus("Completed");
            transactionRepository.save(txn);
        });
    }

    @Transactional
    public CreateDepositResponse finalPaymentForReservationEventBicycle(int reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Waiting_Payment".equalsIgnoreCase(reservation.getStatus())) {
            throw new RuntimeException(
                    "Chỉ có thể thanh toán cuối cho giao dịch đã hoàn thành kiểm định (Waiting_Payment). Trạng thái hiện tại: "
                            + reservation.getStatus());
        }

        if (reservation.getBuyer() == null || reservation.getBuyer().getUserId() != buyer.getUserId()) {
            throw new RuntimeException("Bạn không phải người mua của giao dịch này.");
        }

        EventBicycle eventBicycle = eventBicycleRepository.findById(reservation.getEventBicycle().getEventBikeId())
                .orElseThrow(()  -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        double price = eventBicycle.getPrice();
        double depositAmount = reservation.getDepositAmount() != null ? reservation.getDepositAmount() : 0;
        double remainingAmount = price - depositAmount;

        if (remainingAmount <= 0) {
            throw new RuntimeException(
                    "Số tiền còn lại không hợp lệ (" + remainingAmount + "). Giao dịch có thể đã thanh toán đủ.");
        }

        Wallet buyerWallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder().user(buyer).username(username).balance(0.0).type("User").build();
            return walletRepository.save(newWallet);
        });

        // TRƯỜNG HỢP 1: VÍ ĐỦ TIỀN → TRỪ THẲNG
        if (buyerWallet.getBalance() >= remainingAmount) {
            completeReservation(reservation, eventBicycle, remainingAmount, username);
            return CreateDepositResponse.builder()
                    .deposit(null)
                    .paymentUrl(null)
                    .message("Thanh toán thành công! Đã trừ " + (long) remainingAmount
                            + " VND từ ví. Giao dịch hoàn tất.")
                    .build();
        }

        // TRƯỜNG HỢP 2: VÍ KHÔNG ĐỦ → TẠO VNPAY URL
        long amountNeeded = (long) Math.ceil(remainingAmount - buyerWallet.getBalance());
        String customReturnUrl = vnpayReturnUrl + "?reservationId=" + reservationId;
        String paymentUrl = vnPayService.createOrder(
                amountNeeded,
                username + "|finalpaymentEventbicycle|" + reservationId,
                customReturnUrl,
                null);

        return CreateDepositResponse.builder()
                .deposit(null)
                .paymentUrl(paymentUrl)
                .message("Ví không đủ tiền. Vui lòng thanh toán thêm " + amountNeeded
                        + " VND qua VNPay để hoàn tất giao dịch.")
                .build();
    }

    @Transactional
    public void confirmFinalPaymentForEventBicycle(int reservationId, String username, double vnpayAmount) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!"Waiting_Payment".equalsIgnoreCase(reservation.getStatus())) {
            // Đã được xử lý rồi (idempotent)
            return;
        }

        EventBicycle eventBicycle = eventBicycleRepository.findById(reservation.getEventBicycle().getEventBikeId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        double price = eventBicycle.getPrice();
        double depositAmount = reservation.getDepositAmount() != null ? reservation.getDepositAmount() : 0;
        double remainingAmount = price - depositAmount;

        // Nạp tiền từ VNPay vào ví Buyer
        Wallet buyerWallet = walletRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        buyerWallet.setBalance(buyerWallet.getBalance() + vnpayAmount);
        walletRepository.save(buyerWallet);
        walletTransactionService.createTransaction(buyerWallet, vnpayAmount, "FinalPayment_TopUp",
                "Nạp tiền qua VNPay để thanh toán cuối giao dịch #" + reservationId);

        // Hoàn tất giao dịch (trừ phần còn lại ra khỏi ví)
        completeReservation(reservation, eventBicycle, remainingAmount, username);
    }

    private void completeReservation(Reservation reservation, EventBicycle eventBicycle, double remainingAmount,
                                     String buyerUsername) {
        Wallet buyerWallet = walletRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình System Wallet"));

        if (buyerWallet.getBalance() < remainingAmount) {
            throw new RuntimeException(
                    "Ví không đủ tiền để hoàn tất thanh toán. Số dư hiện tại: " + (long) buyerWallet.getBalance()
                            + " VND, cần thêm: " + (long) (remainingAmount - buyerWallet.getBalance()) + " VND.");
        }

        // Chuyển tiền Buyer → System Wallet
        buyerWallet.setBalance(buyerWallet.getBalance() - remainingAmount);
        systemWallet.setBalance(systemWallet.getBalance() + remainingAmount);
        walletRepository.save(buyerWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(buyerWallet, remainingAmount, "FinalPayment",
                "Thanh toán cuối giao dịch xe đạp #" + eventBicycle.getEventBikeId());

        // Cập nhật trạng thái
        reservation.setStatus("Completed");
        reservationRepository.save(reservation);

        BikeListing bikeListing = eventBicycle.getListing();

        if (bikeListing != null) {
            bikeListing.setStatus("Sold");
            bikeListingRepository.save(bikeListing);
        }

        eventBicycle.setStatus("Sold");
        eventBicycleRepository.save(eventBicycle);

        transactionRepository.findByReservation_ReservationId(reservation.getReservationId()).ifPresent(txn -> {
            txn.setStatus("Completed");
            transactionRepository.save(txn);
        });
    }

    private ReservationResponse toReservationResponseSafe(Reservation reservation) {
        ReservationResponse response = reservationMapper.toReservationResponse(reservation);

        if (reservation.getListing() != null && reservation.getDepositAmount() != null) {
            double listingPrice = reservation.getListing().getPrice();
            double depositAmount = reservation.getDepositAmount();
            response.setRemainingAmount(listingPrice - depositAmount);
        } else if (reservation.getEventBicycle() != null && reservation.getDepositAmount() != null) {
            double eventPrice = reservation.getEventBicycle().getPrice();
            double depositAmount = reservation.getDepositAmount();
            response.setRemainingAmount(eventPrice - depositAmount);
        }

        // Bổ sung seller info và fallback ảnh/tên từ EventBicycle khi listing null
        if (reservation.getEventBicycle() != null && reservation.getListing() == null) {
            EventBicycle eb = reservation.getEventBicycle();

            // Gán dự phòng thông tin để UI luôn hiển thị được
            if (response.getListingTitle() == null) {
                response.setListingTitle(eb.getTitle());
            }
            if (response.getListingImage() == null) {
                response.setListingImage(eb.getImage_url());
            }

            if (eb.getSeller() != null) {
                response.setSellerName(eb.getSeller().getFullName() != null
                        ? eb.getSeller().getFullName()
                        : eb.getSellerName());
                response.setSellerId(eb.getSeller().getUserId());
            } else if (eb.getSellerName() != null) {
                response.setSellerName(eb.getSellerName());
            }
        }

        return response;
    }
}