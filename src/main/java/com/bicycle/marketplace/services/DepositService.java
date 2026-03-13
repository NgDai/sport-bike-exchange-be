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

        // Kiểm tra đã đặt cọc chưa
        depositRepository.findByUserAndListing(user, listing).ifPresent(d -> {
            throw new AppException(ErrorCode.DEPOSIT_ALREADY_EXISTS);
        });

        // Tính tiền cọc từ SystemConfig
        double amount = calculateDepositAmount(listing.getPrice());

        // Kiểm tra ví
        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(username)
                    .balance(0.0)
                    .type("User")
                    .build();
            return walletRepository.save(newWallet);
        });

        // NẾU VÍ KHÔNG ĐỦ
        if (wallet.getBalance() < amount) {
            Deposit deposit = Deposit.builder()
                    .user(user)
                    .listing(listing)
                    .type("Deposit")
                    .amount(amount)
                    .status("Waiting_Payment")
                    .build();
            Deposit savedDeposit = depositRepository.save(deposit);

            Reservation reservation = Reservation.builder()
                    .buyer(user)
                    .listing(listing)
                    .depositAmount(amount)
                    .deposit(savedDeposit)
                    .status("Waiting_Payment")
                    .build();
            reservation = reservationRepository.save(reservation);

            TransactionCreationRequest txnRequest = new TransactionCreationRequest();
            txnRequest.setDepositId(savedDeposit.getDepositId());
            txnRequest.setReservationId(reservation.getReservationId());
            txnRequest.setListingId(listing.getListingId());
            txnRequest.setBuyerId(user.getUserId());
            txnRequest.setSellerId(listing.getSeller().getUserId());
            txnRequest.setAmount(amount);
            txnRequest.setActualPrice(listing.getPrice());
            transactionService.createTransaction(txnRequest);

            long amountNeeded = (long) Math.ceil(amount - wallet.getBalance());
            String customReturnUrl = vnpayReturnUrl + "?depositId=" + savedDeposit.getDepositId();
            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|deposit|" + savedDeposit.getDepositId(),
                    customReturnUrl,
                    null
            );

            return CreateDepositResponse.builder()
                    .deposit(null)
                    .paymentUrl(paymentUrl)
                    .message("Số dư không đủ. Tiền cọc: " + (long) amount + " VND. Vui lòng thanh toán " + amountNeeded + " VND.")
                    .build();
        }

        // NẾU VÍ ĐỦ
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance() - amount);
        systemWallet.setBalance(systemWallet.getBalance() + amount);
        walletRepository.save(wallet);
        walletRepository.save(systemWallet);

        Deposit deposit = Deposit.builder()
                .user(user)
                .listing(listing)
                .type("Deposit")
                .amount(amount)
                .status("Paid")
                .build();
        Deposit savedDeposit = depositRepository.save(deposit);

        Reservation reservation = Reservation.builder()
                .buyer(user)
                .listing(listing)
                .depositAmount(amount)
                .deposit(savedDeposit)
                .status("Deposited")
                .build();
        reservation = reservationRepository.save(reservation);

        // Sinh Transaction qua TransactionService
        TransactionCreationRequest txnRequest = new TransactionCreationRequest();
        txnRequest.setDepositId(savedDeposit.getDepositId());
        txnRequest.setReservationId(reservation.getReservationId());
        txnRequest.setListingId(listing.getListingId());
        txnRequest.setBuyerId(user.getUserId());
        txnRequest.setSellerId(listing.getSeller().getUserId());
        txnRequest.setAmount(amount);
        txnRequest.setActualPrice(listing.getPrice());
        transactionService.createTransaction(txnRequest);

        walletTransactionService.createTransaction(wallet, amount, "Deposit", "Đặt cọc xe đạp");

        return CreateDepositResponse.builder()
                .deposit(depositMapper.toDepositResponse(savedDeposit))
                .paymentUrl(null)
                .message("Đặt cọc thành công! Đã trừ " + (long) amount + " VND từ ví.")
                .build();
    }

    @Transactional
    public void confirmDepositPayment(int depositId, String username, double vnpayAmount) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));

        if (!"Waiting_Payment".equals(deposit.getStatus())) {
            return;
        }

        Users user = deposit.getUser();
        BikeListing listing = deposit.getListing();
        double depositAmount = deposit.getAmount();

        deposit.setStatus("Paid");
        deposit = depositRepository.save(deposit);

        Wallet userWallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(username)
                    .balance(0.0)
                    .type("User")
                    .build();
            return walletRepository.save(newWallet);
        });

        userWallet.setBalance(userWallet.getBalance() + vnpayAmount);
        walletRepository.save(userWallet);
        walletTransactionService.createTransaction(userWallet, vnpayAmount, "Deposit_TopUp", "Nạp tiền qua VNPay để đặt cọc");

        userWallet.setBalance(userWallet.getBalance() - depositAmount);
        walletRepository.save(userWallet);
        walletTransactionService.createTransaction(userWallet, depositAmount, "Deposit", "Đặt cọc xe đạp");

        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        systemWallet.setBalance(systemWallet.getBalance() + depositAmount);
        walletRepository.save(systemWallet);

        Reservation reservation = reservationRepository.findByDeposit_DepositId(depositId)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus("Deposited");
        reservationRepository.save(reservation);

        Transaction transaction = transactionRepository.findByDeposit_DepositId(depositId)
                .orElse(null);
        if (transaction != null) {
            transaction.setStatus("Paid");
            transactionRepository.save(transaction);
        }
    }
}
