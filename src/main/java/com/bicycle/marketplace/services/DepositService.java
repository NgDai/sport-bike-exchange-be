package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.DepositCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.response.DepositResponse;
import com.bicycle.marketplace.dto.response.CreateDepositResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DepositMapper;
import com.bicycle.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositService {

    @Autowired
    private IDepositRepository depositRepository;
    @Autowired
    private DepositMapper depositMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IBikeListingRepository bikeListingRepository;
    @Autowired
    private IEventBicycleRepository eventBicycleRepository;
    @Autowired
    private IWalletRepository walletRepository;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private IReservationRepository reservationRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ITransactionRepository transactionRepository;
    @Autowired
    private WalletTransactionService walletTransactionService;
    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Value("${vnpay.returnUrl}")
    private String vnpayReturnUrl;

    public double calculateDepositAmount(double price) {
        double depositPercent = systemConfigRepository.findByKey("Phí_Cọc")
                .map(SystemConfig::getValue)
                .orElse(10.0); // Mặc định 10%
        return price * depositPercent / 100.0;
    }

    public Integer getEventIdByDepositId(int depositId) {
        return depositRepository.findById(depositId)
                .map(deposit -> (deposit.getEventBicycle() != null && deposit.getEventBicycle().getEvent() != null)
                        ? deposit.getEventBicycle().getEvent().getEventId() : null)
                .orElse(null);
    }

    // ==========================================
    // HÀM MỚI THÊM ĐỂ ĐIỀU HƯỚNG VỀ BIKEDETAIL
    // ==========================================
    public Integer getListingIdByDepositId(int depositId) {
        return depositRepository.findById(depositId)
                .map(deposit -> deposit.getListing() != null ? deposit.getListing().getListingId() : null)
                .orElse(null);
    }

    @Transactional
    public CreateDepositResponse createDepositViaVNPay(int listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BikeListing listing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        // 1. KIỂM TRA TRẠNG THÁI BÀI ĐĂNG (BIKELISTING)
        if ("Deposited".equals(listing.getStatus()) || "Sold".equals(listing.getStatus())) {
            throw new RuntimeException("Xe này đã được đặt cọc hoặc đã bán.");
        }

        // 2. XỬ LÝ LỖI KẸT GIAO DỊCH (Hủy thanh toán lần trước)
        var existingDepositOpt = depositRepository.findByUserAndListing(user, listing);
        if (existingDepositOpt.isPresent()) {
            Deposit existingDeposit = existingDepositOpt.get();
            if ("Paid".equalsIgnoreCase(existingDeposit.getStatus())) {
                throw new RuntimeException("Bạn đã thanh toán cọc cho chiếc xe này rồi.");
            } else if ("Waiting_Payment".equalsIgnoreCase(existingDeposit.getStatus())) {
                // Xóa rác cũ để làm lại giao dịch mới
                transactionRepository.findByDeposit_DepositId(existingDeposit.getDepositId())
                        .ifPresent(transactionRepository::delete);
                reservationRepository.findByDeposit_DepositId(existingDeposit.getDepositId())
                        .ifPresent(reservationRepository::delete);
                depositRepository.delete(existingDeposit);
            }
        }

        double amount = calculateDepositAmount(listing.getPrice());
        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            return walletRepository
                    .save(Wallet.builder().user(user).username(username).balance(0.0).type("User").build());
        });

        // 3. TRƯỜNG HỢP VÍ KHÔNG ĐỦ TIỀN -> GỌI VNPAY
        if (wallet.getBalance() < amount) {
            // Khóa bài đăng lại để người khác không mua được (Bỏ theo yêu cầu: Đợi thanh toán xong mới đổi trạng thái)
            // listing.setStatus("Waiting_Payment");
            // bikeListingRepository.save(listing);

            Deposit deposit = depositRepository.save(Deposit.builder()
                    .user(user).listing(listing).type("Deposit").amount(amount).status("Waiting_Payment").build());

            Reservation reservation = reservationRepository.save(Reservation.builder()
                    .buyer(user).listing(listing).depositAmount(amount).deposit(deposit).status("Waiting_Payment")
                    .build());

            TransactionCreationRequest txnRequest = new TransactionCreationRequest();
            txnRequest.setDepositId(deposit.getDepositId());
            txnRequest.setReservationId(reservation.getReservationId());
            txnRequest.setListingId(listing.getListingId());
            txnRequest.setBuyerId(user.getUserId());
            txnRequest.setSellerId(listing.getSeller() != null ? listing.getSeller().getUserId() : null);
            txnRequest.setAmount(amount);
            txnRequest.setActualPrice(listing.getPrice());
            transactionService.createTransaction(txnRequest);

            long amountNeeded = (long) Math.ceil(amount - wallet.getBalance());
            String customReturnUrl = vnpayReturnUrl + "?depositId=" + deposit.getDepositId();
            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|deposit|" + deposit.getDepositId(),
                    customReturnUrl, null);

            return CreateDepositResponse.builder()
                    .deposit(null)
                    .paymentUrl(paymentUrl)
                    .message("Vui lòng thanh toán thêm " + amountNeeded + " VND qua VNPay.")
                    .build();
        }

        // 4. TRƯỜNG HỢP VÍ ĐỦ TIỀN -> TRỪ LUÔN VÀ HOÀN TẤT ĐẶT CỌC
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình System Wallet"));

        // Chuyển tiền ví User -> ví System
        wallet.setBalance(wallet.getBalance() - amount);
        systemWallet.setBalance(systemWallet.getBalance() + amount);
        walletRepository.save(wallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(wallet, amount, "Deposit",
                "Đặt cọc xe đạp #" + listing.getListingId());

        // ĐỔI TRẠNG THÁI BÀI ĐĂNG (ẨN KHỎI SÀN)
        listing.setStatus("Deposited");
        bikeListingRepository.save(listing);

        Deposit deposit = depositRepository.save(Deposit.builder()
                .user(user).listing(listing).type("Deposit").amount(amount).status("Paid").build());

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .buyer(user).listing(listing).depositAmount(amount).deposit(deposit).status("Deposited").build());

        TransactionCreationRequest txnRequest = new TransactionCreationRequest();
        txnRequest.setDepositId(deposit.getDepositId());
        txnRequest.setReservationId(reservation.getReservationId());
        txnRequest.setListingId(listing.getListingId());
        txnRequest.setBuyerId(user.getUserId());
        txnRequest.setSellerId(listing.getSeller() != null ? listing.getSeller().getUserId() : null);
        txnRequest.setAmount(amount);
        txnRequest.setActualPrice(listing.getPrice());
        transactionService.createTransaction(txnRequest);

        return CreateDepositResponse.builder()
                .deposit(depositMapper.toDepositResponse(deposit))
                .paymentUrl(null)
                .message("Đặt cọc thành công! Hệ thống đã trừ " + (long) amount + " VND từ ví.")
                .build();
    }

    // 5. HÀM XỬ LÝ KHI VNPAY THANH TOÁN THÀNH CÔNG (Được Controller gọi)
    @Transactional
    public void confirmDepositPayment(int depositId, String username, double vnpayAmount) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch đặt cọc này"));

        if (!"Waiting_Payment".equals(deposit.getStatus()))
            return;

        Users user = deposit.getUser();
        BikeListing listing = deposit.getListing();
        double depositAmount = deposit.getAmount();

        Wallet userWallet = walletRepository.findByUsername(username).orElseThrow();
        Wallet systemWallet = walletRepository.findByUsername("System").orElseThrow();

        // Nạp tiền VNPay vào ví User
        userWallet.setBalance(userWallet.getBalance() + vnpayAmount);
        walletRepository.save(userWallet);
        walletTransactionService.createTransaction(userWallet, vnpayAmount, "Deposit_TopUp",
                "Nạp tiền qua VNPay để cọc");

        // Trừ tiền cọc từ ví User sang ví System
        userWallet.setBalance(userWallet.getBalance() - depositAmount);
        systemWallet.setBalance(systemWallet.getBalance() + depositAmount);
        walletRepository.save(userWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(userWallet, depositAmount, "Deposit",
                "Thanh toán cọc xe #" + listing.getListingId());

        // KIỂM TRA LẠI TRẠNG THÁI XE (TRÁNH TRANH CHẤP KHI NHIỀU NGƯỜI CÙNG THANH TOÁN)
        if (!"Available".equals(listing.getStatus())) {
            // Xe đã bị người khác cọc hoặc mua mất trong lúc user này đang ở cổng thanh toán
            deposit.setStatus("Cancelled_AlreadyDeposited");
            depositRepository.save(deposit);

            reservationRepository.findByDeposit_DepositId(depositId).ifPresent(res -> {
                res.setStatus("Cancelled");
                reservationRepository.save(res);
            });
            // Tiền vẫn ở trong ví User (vì đã nạp ở trên), không trừ thêm gì nữa.
            return;
        }

        // ĐỔI TRẠNG THÁI BÀI ĐĂNG ĐỂ ẨN KHỎI SÀN
        listing.setStatus("Deposited");
        bikeListingRepository.save(listing);

        // Cập nhật trạng thái các bảng liên quan
        deposit.setStatus("Paid");
        depositRepository.save(deposit);

        reservationRepository.findByDeposit_DepositId(depositId).ifPresent(res -> {
            res.setStatus("Deposited");
            reservationRepository.save(res);
        });

        transactionRepository.findByDeposit_DepositId(depositId).ifPresent(txn -> {
            txn.setStatus("Paid");
            transactionRepository.save(txn);
        });
    }

    // 6. HÀM XỬ LÝ KHI NGƯỜI DÙNG HỦY GIAO DỊCH TRÊN VNPAY (Được Controller gọi)
    @Transactional
    public void cancelDepositPayment(int depositId) {
        Deposit deposit = depositRepository.findById(depositId).orElse(null);
        if (deposit == null || !"Waiting_Payment".equals(deposit.getStatus())) {
            return;
        }

        // 1. Nhả lại xe trên sàn (Nếu xe có tồn tại trên sàn)
        BikeListing listing = deposit.getListing();
        if (listing != null && "Waiting_Payment".equals(listing.getStatus())) {
            listing.setStatus("Available");
            bikeListingRepository.save(listing);
        }

        // 2. Nhả lại xe trong sự kiện (Nếu xe đang tham gia sự kiện)
        EventBicycle eventBicycle = deposit.getEventBicycle();
        if (eventBicycle != null && "Waiting_Payment".equals(eventBicycle.getStatus())) {
            eventBicycle.setStatus("Available");
            eventBicycleRepository.save(eventBicycle);
        }

        // 3. Xóa các record rác (Hóa đơn, Cọc tạm)
        transactionRepository.findByDeposit_DepositId(depositId).ifPresent(transactionRepository::delete);
        reservationRepository.findByDeposit_DepositId(depositId).ifPresent(reservationRepository::delete);
        depositRepository.delete(deposit);
    }

    @Transactional
    public DepositResponse createDeposit(int listingId, DepositCreationRequest request) {
        return null;
    }

    @Transactional
    public CreateDepositResponse createDepositViaVNPayForEvent(int eventBikeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        EventBicycle eventBicycle = eventBicycleRepository.findById(eventBikeId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_BICYCLE_NOT_FOUND));

        // 1. KIỂM TRA TRẠNG THÁI BÀI ĐĂNG (BIKELISTING)
        if ("Deposited".equals(eventBicycle.getStatus())) {
            throw new RuntimeException("Xe này đã được đặt cọc.");
        }

        // 2. XỬ LÝ LỖI KẸT GIAO DỊCH (Hủy thanh toán lần trước)
        var existingDepositOpt = depositRepository.findByUserAndEventBicycle(user, eventBicycle);
        if (existingDepositOpt.isPresent()) {
            Deposit existingDeposit = existingDepositOpt.get();
            if ("Paid".equalsIgnoreCase(existingDeposit.getStatus())) {
                throw new RuntimeException("Bạn đã thanh toán cọc cho chiếc xe này rồi.");
            } else if ("Waiting_Payment".equalsIgnoreCase(existingDeposit.getStatus())) {
                // Xóa rác cũ để làm lại giao dịch mới
                transactionRepository.findByDeposit_DepositId(existingDeposit.getDepositId())
                        .ifPresent(transactionRepository::delete);
                reservationRepository.findByDeposit_DepositId(existingDeposit.getDepositId())
                        .ifPresent(reservationRepository::delete);
                depositRepository.delete(existingDeposit);
            }
        } else if ("Waiting_Payment".equals(eventBicycle.getStatus())) {
            // Nếu người khác đang thanh toán xe này
            throw new RuntimeException(
                    "Xe này đang trong quá trình thanh toán bởi người khác. Vui lòng thử lại sau vài phút.");
        }

        double amount = calculateDepositAmount(eventBicycle.getPrice());
        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            return walletRepository.save(Wallet.builder().user(user).username(username).balance(0.0).type("User").build());
        });

        // 3. TRƯỜNG HỢP VÍ KHÔNG ĐỦ TIỀN -> GỌI VNPAY
        if (wallet.getBalance() < amount) {
            // Khóa bài đăng lại để người khác không mua được (Bỏ theo yêu cầu: Đợi thanh toán xong mới đổi trạng thái)
            // eventBicycle.setStatus("Waiting_Payment");
            // eventBicycleRepository.save(eventBicycle);

            Deposit deposit = depositRepository.save(Deposit.builder()
                    .user(user).listing(eventBicycle.getListing()).eventBicycle(eventBicycle).type("Deposit").amount(amount).status("Waiting_Payment").build());

            Reservation reservation = reservationRepository.save(Reservation.builder()
                    .buyer(user).listing(eventBicycle.getListing()).eventBicycle(eventBicycle).depositAmount(amount).deposit(deposit).status("Waiting_Payment").build());

            TransactionCreationRequest txnRequest = new TransactionCreationRequest();
            txnRequest.setDepositId(deposit.getDepositId());
            txnRequest.setReservationId(reservation.getReservationId());
            if (eventBicycle.getListing() != null) {
                txnRequest.setListingId(eventBicycle.getListing().getListingId());
            } else {
                txnRequest.setListingId(null);
            }
            txnRequest.setEventBikeId(eventBikeId);
            txnRequest.setBuyerId(user.getUserId());
            txnRequest.setSellerId(eventBicycle.getSeller() != null ? eventBicycle.getSeller().getUserId() : null);
            txnRequest.setAmount(amount);
            txnRequest.setActualPrice(eventBicycle.getPrice());
            transactionService.createTransaction(txnRequest);

            long amountNeeded = (long) Math.ceil(amount - wallet.getBalance());
            String customReturnUrl = vnpayReturnUrl + "?depositId=" + deposit.getDepositId();
            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|eventdeposit|" + deposit.getDepositId(),
                    customReturnUrl, null
            );

            return CreateDepositResponse.builder()
                    .deposit(null)
                    .paymentUrl(paymentUrl)
                    .message("Vui lòng thanh toán thêm " + amountNeeded + " VND qua VNPay.")
                    .build();
        }

        // 4. TRƯỜNG HỢP VÍ ĐỦ TIỀN -> TRỪ LUÔN VÀ HOÀN TẤT ĐẶT CỌC
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình System Wallet"));

        // Chuyển tiền ví User -> ví System
        wallet.setBalance(wallet.getBalance() - amount);
        systemWallet.setBalance(systemWallet.getBalance() + amount);
        walletRepository.save(wallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(wallet, amount, "Deposit", "Đặt cọc xe đạp #" + eventBikeId);

        // ĐỔI TRẠNG THÁI BÀI ĐĂNG (ẨN KHỎI SÀN)
        eventBicycle.setStatus("Deposited");
        eventBicycleRepository.save(eventBicycle);

        Deposit deposit = depositRepository.save(Deposit.builder()
                .user(user).listing(eventBicycle.getListing()).eventBicycle(eventBicycle).type("Deposit").amount(amount).status("Paid").build());

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .buyer(user).listing(eventBicycle.getListing()).eventBicycle(eventBicycle).depositAmount(amount).deposit(deposit).status("Deposited").build());

        TransactionCreationRequest txnRequest = new TransactionCreationRequest();
        txnRequest.setDepositId(deposit.getDepositId());
        txnRequest.setReservationId(reservation.getReservationId());
        if (eventBicycle.getListing() != null) {
            txnRequest.setListingId(eventBicycle.getListing().getListingId());
        } else {
            txnRequest.setListingId(null);
        }
        txnRequest.setEventBikeId(eventBikeId);
        txnRequest.setBuyerId(user.getUserId());
        txnRequest.setSellerId(eventBicycle.getSeller() != null ? eventBicycle.getSeller().getUserId() : null);
        txnRequest.setAmount(amount);
        txnRequest.setActualPrice(eventBicycle.getPrice());
        transactionService.createTransaction(txnRequest);

        return CreateDepositResponse.builder()
                .deposit(depositMapper.toDepositResponse(deposit))
                .paymentUrl(null)
                .message("Đặt cọc thành công! Hệ thống đã trừ " + (long) amount + " VND từ ví.")
                .build();
    }

    // 5. HÀM XỬ LÝ KHI VNPAY THANH TOÁN THÀNH CÔNG (Được Controller gọi)
    @Transactional
    public void confirmDepositPaymentForEvent(int depositId, String username, double vnpayAmount) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch đặt cọc này"));

        if (!"Waiting_Payment".equals(deposit.getStatus()))
            return;

        Users user = deposit.getUser();
        BikeListing listing = deposit.getListing();
        EventBicycle eventBicycle = deposit.getEventBicycle();
        double depositAmount = deposit.getAmount();

        Wallet userWallet = walletRepository.findByUsername(username).orElseThrow();
        Wallet systemWallet = walletRepository.findByUsername("System").orElseThrow();

        // Nạp tiền VNPay vào ví User
        userWallet.setBalance(userWallet.getBalance() + vnpayAmount);
        walletRepository.save(userWallet);
        walletTransactionService.createTransaction(userWallet, vnpayAmount, "Deposit_TopUp",
                "Nạp tiền qua VNPay để cọc");

        // Trừ tiền cọc từ ví User sang ví System
        userWallet.setBalance(userWallet.getBalance() - depositAmount);
        systemWallet.setBalance(systemWallet.getBalance() + depositAmount);
        walletRepository.save(userWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(userWallet, depositAmount, "Deposit",
                "Thanh toán cọc xe #" + eventBicycle.getEventBikeId());

        // KIỂM TRA LẠI TRẠNG THÁI XE (TRÁNH TRANH CHẤP)
        if (!"Available_in_event".equals(eventBicycle.getStatus())) {
            deposit.setStatus("Cancelled_AlreadyDeposited");
            depositRepository.save(deposit);

            reservationRepository.findByDeposit_DepositId(depositId).ifPresent(res -> {
                res.setStatus("Cancelled");
                reservationRepository.save(res);
            });
            return;
        }

        // ĐỔI TRẠNG THÁI BÀI ĐĂNG ĐỂ ẨN KHỎI SÀN
        if (listing != null) {
            listing.setStatus("Deposited");
            bikeListingRepository.save(listing);
        }
        eventBicycle.setStatus("Deposited");
        eventBicycleRepository.save(eventBicycle);

        // Cập nhật trạng thái các bảng liên quan
        deposit.setStatus("Paid");
        depositRepository.save(deposit);

        reservationRepository.findByDeposit_DepositId(depositId).ifPresent(res -> {
            res.setStatus("Deposited");
            reservationRepository.save(res);
        });

        transactionRepository.findByDeposit_DepositId(depositId).ifPresent(txn -> {
            txn.setStatus("Paid");
            transactionRepository.save(txn);
        });
    }

    // 6. HÀM XỬ LÝ KHI NGƯỜI DÙNG HỦY GIAO DỊCH TRÊN VNPAY (Được Controller gọi)
    @Transactional
    public void cancelDepositPaymentForEvent(int depositId) {
        Deposit deposit = depositRepository.findById(depositId).orElse(null);
        if (deposit == null || !"Waiting_Payment".equals(deposit.getStatus())) {
            return;
        }

        // Nhả lại xe cho người khác mua
        EventBicycle eventBicycle = deposit.getEventBicycle();
        if ("Waiting_Payment".equals(eventBicycle.getStatus())) {
            eventBicycle.setStatus("Available_in_event");
            eventBicycleRepository.save(eventBicycle);
        }

        // Xóa các record rác
        transactionRepository.findByDeposit_DepositId(depositId).ifPresent(transactionRepository::delete);
        reservationRepository.findByDeposit_DepositId(depositId).ifPresent(reservationRepository::delete);
        depositRepository.delete(deposit);
    }
}