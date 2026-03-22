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

    public ReservationResponse createReservation(int bikeListingId, int eventBikeId, ReservationCreationRequest request) {
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
        Users user =  userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Reservation> reservations = reservationRepository.findByBuyer_UserIdAndEventBicycleNotNull(user.getUserId());
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

        if (!reservation.getStatus().equalsIgnoreCase("Deposited")
                && !reservation.getStatus().equalsIgnoreCase("Reserved")
                && !reservation.getStatus().equalsIgnoreCase("Scheduled")) {
            throw new RuntimeException("Không thể hủy reservation ở trạng thái: " + reservation.getStatus());
        }

        BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        // 1. Cập nhật Transaction liên quan (nếu có) và xóa Deposit trước khi xóa Reservation
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
        listing.setStatus("Available");
        bikeListingRepository.save(listing);

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

        if (reservation.getListing().getSeller().getUserId() != seller.getUserId()) {
            throw new RuntimeException("Chỉ người bán mới có quyền yêu cầu hủy giao dịch này.");
        }

        if (!reservation.getStatus().equalsIgnoreCase("Deposited")
                && !reservation.getStatus().equalsIgnoreCase("Reserved")
                && !reservation.getStatus().equalsIgnoreCase("Scheduled")) {
            throw new RuntimeException("Không thể yêu cầu hủy reservation ở trạng thái: " + reservation.getStatus());
        }

        reservation.setStatus("Pending_Cancel");
        reservation.setCancelDescription(request.getCancelDescription());
        reservation.setCancelImage(request.getCancelImage());
        reservationRepository.save(reservation);

        // Đồng bộ Transaction nếu có (chờ admin duyệt nên đổi status của transation cũng hợp lý, hoặc giữ nguyên nhưng đổi Reservation là đủ)
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
        // Vì status hiện tại là Pending_Cancel nên ta gọi logic giống cancelReservation, nhưng bypass cái check status đó
        BikeListing listing = bikeListingRepository.findById(reservation.getListing().getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

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

        listing.setStatus("Available");
        bikeListingRepository.save(listing);

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
        // Nếu có inspector thì có thể là Scheduled, nếu không có inspector và có meeting info thì là Reserved, không तो là Deposited
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
            throw new RuntimeException("Chỉ có thể hoàn tiền cho giao dịch có trạng thái Inspection_Failed. Trạng thái hiện tại: " + reservation.getStatus());
        }

        // Kiểm tra quyền: chỉ buyer hoặc admin mới được thực hiện
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isBuyer = reservation.getBuyer() != null
                && reservation.getBuyer().getUsername().equals(username);
        if (!isAdmin && !isBuyer) {
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
                "Hoàn tiền cọc do kiểm định thất bại - Giao dịch #" + reservationId);

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

        // Đảm bảo listing về Available (InspectionReportService đã làm, đây là safety net)
        BikeListing listing = reservation.getListing();
        if (listing != null && !"Available".equalsIgnoreCase(listing.getStatus())) {
            listing.setStatus("Available");
            bikeListingRepository.save(listing);
        }

        // Đặt reservation thành Cancelled (đã hoàn tiền xong)
        reservation.setStatus("Cancelled");
        reservationRepository.save(reservation);

        return "Đã hoàn tiền cọc " + amount.longValue() + " VND thành công cho người mua. Giao dịch đã bị hủy.";
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
            throw new RuntimeException("Chỉ có thể thanh toán cuối cho giao dịch đã hoàn thành kiểm định (Waiting_Payment). Trạng thái hiện tại: " + reservation.getStatus());
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
            throw new RuntimeException("Số tiền còn lại không hợp lệ (" + remainingAmount + "). Giao dịch có thể đã thanh toán đủ.");
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
                    .message("Thanh toán thành công! Đã trừ " + (long) remainingAmount + " VND từ ví. Giao dịch hoàn tất.")
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
                .message("Ví không đủ tiền. Vui lòng thanh toán thêm " + amountNeeded + " VND qua VNPay để hoàn tất giao dịch.")
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
    
    private void completeReservation(Reservation reservation, BikeListing listing, double remainingAmount, String buyerUsername) {
        Wallet buyerWallet = walletRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình System Wallet"));

        if (buyerWallet.getBalance() < remainingAmount) {
            throw new RuntimeException("Ví không đủ tiền để hoàn tất thanh toán. Số dư hiện tại: " + (long) buyerWallet.getBalance() + " VND, cần thêm: " + (long) (remainingAmount - buyerWallet.getBalance()) + " VND.");
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
                        ? eb.getSeller().getFullName() : eb.getSellerName());
                response.setSellerId(eb.getSeller().getUserId());
            } else if (eb.getSellerName() != null) {
                response.setSellerName(eb.getSellerName());
            }
        }

        return response;
    }
}