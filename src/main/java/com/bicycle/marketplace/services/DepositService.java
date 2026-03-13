package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.DepositCreationRequest;
import com.bicycle.marketplace.dto.request.TransactionCreationRequest;
import com.bicycle.marketplace.dto.request.DepositUpdateRequest;
import com.bicycle.marketplace.dto.response.DepositResponse;
import com.bicycle.marketplace.dto.response.CreateDepositResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.DepositMapper;
import com.bicycle.marketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public DepositResponse createDeposit(int listingId, DepositCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        String username = authentication.getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BikeListing listing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
        
        depositRepository.findByUserAndListing(user, listing).ifPresent(d -> {
            throw new AppException(ErrorCode.DEPOSIT_ALREADY_EXISTS);
        });

        Deposit deposit = depositMapper.toDeposit(request);
        deposit.setUser(user);
        deposit.setListing(listing);
        if (deposit.getStatus() == null || deposit.getStatus().isBlank()) {
            deposit.setStatus("Paid");
        }
        Deposit savedDeposit = depositRepository.save(deposit);

        Reservation reservation = Reservation.builder()
                .buyer(user)
                .listing(listing)
                .depositAmount(savedDeposit.getAmount())
                .deposit(savedDeposit)
                .status("Deposited")
                .build();
        reservation = reservationRepository.save(reservation);

        TransactionCreationRequest txnRequest = new TransactionCreationRequest();
        txnRequest.setDepositId(savedDeposit.getDepositId());
        txnRequest.setReservationId(reservation.getReservationId());
        txnRequest.setListingId(listing.getListingId());
        txnRequest.setBuyerId(user.getUserId());
        if (listing.getSeller() != null) {
            txnRequest.setSellerId(listing.getSeller().getUserId());
        }
        txnRequest.setAmount(savedDeposit.getAmount());
        txnRequest.setActualPrice(listing.getPrice());
        transactionService.createTransaction(txnRequest);

        return depositMapper.toDepositResponse(savedDeposit);
    }

    public double calculateDepositAmount(double price) {
        double depositPercent = systemConfigRepository.findByKey("Phí_Cọc")
                .map(SystemConfig::getValue)
                .orElse(10.0); // Mặc định 10% nếu chưa cấu hình
        return price * depositPercent / 100.0;
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

        // 1. XỬ LÝ LỖI HỦY THANH TOÁN BỊ KẸT
        var existingDepositOpt = depositRepository.findByUserAndListing(user, listing);
        if (existingDepositOpt.isPresent()) {
            Deposit existingDeposit = existingDepositOpt.get();
            if ("Paid".equalsIgnoreCase(existingDeposit.getStatus())) {
                throw new RuntimeException("Bạn đã thanh toán cọc cho chiếc xe này rồi.");
            } else if ("Waiting_Payment".equalsIgnoreCase(existingDeposit.getStatus())) {
                // Nếu lần trước đang thanh toán dở rồi hủy, ta xóa bản ghi cũ đi để làm lại cái mới
                transactionRepository.findByDeposit_DepositId(existingDeposit.getDepositId())
                        .ifPresent(transactionRepository::delete);
                reservationRepository.findByDeposit_DepositId(existingDeposit.getDepositId())
                        .ifPresent(reservationRepository::delete);
                depositRepository.delete(existingDeposit);
            }
        }

        // 2. Lấy thông tin ví và tính toán
        double amount = calculateDepositAmount(listing.getPrice());
        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            return walletRepository.save(Wallet.builder().user(user).username(username).balance(0.0).type("User").build());
        });

        // 3. NẾU VÍ KHÔNG ĐỦ TIỀN -> Tạo trạng thái Waiting và trả về link VNPay nạp phần thiếu
        if (wallet.getBalance() < amount) {
            Deposit deposit = depositRepository.save(Deposit.builder()
                    .user(user).listing(listing).type("Deposit").amount(amount).status("Waiting_Payment").build());

            Reservation reservation = reservationRepository.save(Reservation.builder()
                    .buyer(user).listing(listing).depositAmount(amount).deposit(deposit).status("Waiting_Payment").build());

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
            // Gắn ID deposit vào URL để lúc VNPay gọi về, ta biết đang xử lý cho deposit nào
            String customReturnUrl = vnpayReturnUrl + "?depositId=" + deposit.getDepositId();

            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|deposit|" + deposit.getDepositId(),
                    customReturnUrl,
                    null
            );

            return CreateDepositResponse.builder()
                    .deposit(null)
                    .paymentUrl(paymentUrl)
                    .message("Số dư không đủ. Vui lòng thanh toán thêm " + amountNeeded + " VND qua VNPay.")
                    .build();
        }

        // 4. NẾU VÍ ĐÃ ĐỦ TIỀN SẴN -> Trừ ví và thanh toán luôn
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình System Wallet"));

        wallet.setBalance(wallet.getBalance() - amount);
        systemWallet.setBalance(systemWallet.getBalance() + amount);
        walletRepository.save(wallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(wallet, amount, "Deposit", "Đặt cọc xe đạp #" + listing.getListingId());

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

//    @Transactional
//    public CreateDepositResponse createDepositViaVNPay(int listingId) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//        String username = authentication.getName();
//        Users user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//        BikeListing listing = bikeListingRepository.findById(listingId)
//                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
//
//        // Kiểm tra đã đặt cọc chưa
//        depositRepository.findByUserAndListing(user, listing).ifPresent(d -> {
//            throw new AppException(ErrorCode.DEPOSIT_ALREADY_EXISTS);
//        });
//
//        // Tính tiền cọc từ SystemConfig
//        double amount = calculateDepositAmount(listing.getPrice());
//
//        // Kiểm tra ví
//        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
//            Wallet newWallet = Wallet.builder()
//                    .user(user)
//                    .username(username)
//                    .balance(0.0)
//                    .type("User")
//                    .build();
//            return walletRepository.save(newWallet);
//        });
//
//        // NẾU VÍ KHÔNG ĐỦ
//        if (wallet.getBalance() < amount) {
//            Deposit deposit = Deposit.builder()
//                    .user(user)
//                    .listing(listing)
//                    .type("Deposit")
//                    .amount(amount)
//                    .status("Waiting_Payment")
//                    .build();
//            Deposit savedDeposit = depositRepository.save(deposit);
//
//            Reservation reservation = Reservation.builder()
//                    .buyer(user)
//                    .listing(listing)
//                    .depositAmount(amount)
//                    .deposit(savedDeposit)
//                    .status("Waiting_Payment")
//                    .build();
//            reservation = reservationRepository.save(reservation);
//
//            TransactionCreationRequest txnRequest = new TransactionCreationRequest();
//            txnRequest.setDepositId(savedDeposit.getDepositId());
//            txnRequest.setReservationId(reservation.getReservationId());
//            txnRequest.setListingId(listing.getListingId());
//            txnRequest.setBuyerId(user.getUserId());
//            txnRequest.setSellerId(listing.getSeller().getUserId());
//            txnRequest.setAmount(amount);
//            txnRequest.setActualPrice(listing.getPrice());
//            transactionService.createTransaction(txnRequest);
//
//            long amountNeeded = (long) Math.ceil(amount - wallet.getBalance());
//            String customReturnUrl = vnpayReturnUrl + "?depositId=" + savedDeposit.getDepositId();
//            String paymentUrl = vnPayService.createOrder(
//                    amountNeeded,
//                    username + "|deposit|" + savedDeposit.getDepositId(),
//                    customReturnUrl,
//                    null
//            );
//
//            return CreateDepositResponse.builder()
//                    .deposit(null)
//                    .paymentUrl(paymentUrl)
//                    .message("Số dư không đủ. Tiền cọc: " + (long) amount + " VND. Vui lòng thanh toán " + amountNeeded + " VND.")
//                    .build();
//        }
//
//        // NẾU VÍ ĐỦ
//        Wallet systemWallet = walletRepository.findByUsername("System")
//                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
//
//        wallet.setBalance(wallet.getBalance() - amount);
//        systemWallet.setBalance(systemWallet.getBalance() + amount);
//        walletRepository.save(wallet);
//        walletRepository.save(systemWallet);
//
//        Deposit deposit = Deposit.builder()
//                .user(user)
//                .listing(listing)
//                .type("Deposit")
//                .amount(amount)
//                .status("Paid")
//                .build();
//        Deposit savedDeposit = depositRepository.save(deposit);
//
//        Reservation reservation = Reservation.builder()
//                .buyer(user)
//                .listing(listing)
//                .depositAmount(amount)
//                .deposit(savedDeposit)
//                .status("Deposited")
//                .build();
//        reservation = reservationRepository.save(reservation);
//
//        // Sinh Transaction qua TransactionService
//        TransactionCreationRequest txnRequest = new TransactionCreationRequest();
//        txnRequest.setDepositId(savedDeposit.getDepositId());
//        txnRequest.setReservationId(reservation.getReservationId());
//        txnRequest.setListingId(listing.getListingId());
//        txnRequest.setBuyerId(user.getUserId());
//        txnRequest.setSellerId(listing.getSeller().getUserId());
//        txnRequest.setAmount(amount);
//        txnRequest.setActualPrice(listing.getPrice());
//        transactionService.createTransaction(txnRequest);
//
//        walletTransactionService.createTransaction(wallet, amount, "Deposit", "Đặt cọc xe đạp");
//
//        return CreateDepositResponse.builder()
//                .deposit(depositMapper.toDepositResponse(savedDeposit))
//                .paymentUrl(null)
//                .message("Đặt cọc thành công! Đã trừ " + (long) amount + " VND từ ví.")
//                .build();
//    }

    @Transactional
    public String confirmDepositPayment(int depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));

        if (!"Waiting_Payment".equals(deposit.getStatus())) {
            return "Giao dịch cọc này đã được xử lý.";
        }

        // Lúc này tiền VNPay đã được nạp vào ví người dùng (Ở hàm VNPay callback)
        // Ta cần trừ tiền trong ví User sang ví System để hoàn tất CỌC
        Users user = deposit.getUser();
        Wallet userWallet = walletRepository.findByUsername(user.getUsername()).orElseThrow();
        Wallet systemWallet = walletRepository.findByUsername("System").orElseThrow();
        double amount = deposit.getAmount();

        userWallet.setBalance(userWallet.getBalance() - amount);
        systemWallet.setBalance(systemWallet.getBalance() + amount);
        walletRepository.save(userWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(userWallet, amount, "Deposit", "Đặt cọc xe đạp #" + deposit.getListing().getListingId());

        // Cập nhật trạng thái hoàn thành
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

        return "Thanh toán cọc thành công. Giao dịch đã chuyển sang chờ Admin xếp lịch!";
    }



//    @Transactional
//    public void confirmDepositPayment(int depositId, String username, double vnpayAmount) {
//        Deposit deposit = depositRepository.findById(depositId)
//                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
//
//        if (!"Waiting_Payment".equals(deposit.getStatus())) {
//            return;
//        }
//
//        Users user = deposit.getUser();
//        BikeListing listing = deposit.getListing();
//        double depositAmount = deposit.getAmount();
//
//        deposit.setStatus("Paid");
//        deposit = depositRepository.save(deposit);
//
//        Wallet userWallet = walletRepository.findByUsername(username).orElseGet(() -> {
//            Wallet newWallet = Wallet.builder()
//                    .user(user)
//                    .username(username)
//                    .balance(0.0)
//                    .type("User")
//                    .build();
//            return walletRepository.save(newWallet);
//        });
//
//        userWallet.setBalance(userWallet.getBalance() + vnpayAmount);
//        walletRepository.save(userWallet);
//        walletTransactionService.createTransaction(userWallet, vnpayAmount, "Deposit_TopUp", "Nạp tiền qua VNPay để đặt cọc");
//
//        userWallet.setBalance(userWallet.getBalance() - depositAmount);
//        walletRepository.save(userWallet);
//        walletTransactionService.createTransaction(userWallet, depositAmount, "Deposit", "Đặt cọc xe đạp");
//
//        Wallet systemWallet = walletRepository.findByUsername("System")
//                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
//        systemWallet.setBalance(systemWallet.getBalance() + depositAmount);
//        walletRepository.save(systemWallet);
//
//        Reservation reservation = reservationRepository.findByDeposit_DepositId(depositId)
//                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
//        reservation.setStatus("Deposited");
//        reservationRepository.save(reservation);
//
//        Transaction transaction = transactionRepository.findByDeposit_DepositId(depositId)
//                .orElse(null);
//        if (transaction != null) {
//            transaction.setStatus("Paid");
//            transactionRepository.save(transaction);
//        }
//    }
}
