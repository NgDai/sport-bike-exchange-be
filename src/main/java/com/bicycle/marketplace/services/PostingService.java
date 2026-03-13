package com.bicycle.marketplace.services;

import com.bicycle.marketplace.dto.request.CreatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingRequest;
import com.bicycle.marketplace.dto.request.UpdatePostingStatusRequest;
import com.bicycle.marketplace.dto.response.CreatePostingResponse;
import com.bicycle.marketplace.dto.response.PostingResponse;
import com.bicycle.marketplace.entities.*;
import com.bicycle.marketplace.exception.AppException;
import com.bicycle.marketplace.exception.ErrorCode;
import com.bicycle.marketplace.mapper.PostingMapper;
import com.bicycle.marketplace.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingService {
    @Value("${vnpay.returnUrl}")
    private String vnpayReturnUrl;

    private final IBikeListingRepository bikeListingRepository;
    private final IBicycleRepository bicycleRepository; // ĐÃ THÊM ĐỂ XÓA XE RÁC
    private final PostingMapper postingMapper;
    private final IUserRepository userRepository;
    private final IBrandRepository brandRepository;
    private final ICategoryRepository categoryRepository;
    private final IWalletRepository walletRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final WalletTransactionService walletTransactionService;
    private final VNPayService vnPayService;

    public double calculateListingFee(double price) {
        double feePercent = systemConfigRepository.findByKey("Phí_Sàn")
                .map(SystemConfig::getValue)
                .orElse(5.0);
        return price * feePercent / 100.0;
    }

    @Transactional
    public CreatePostingResponse createPosting(CreatePostingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getPhone() == null || user.getPhone().trim().isEmpty() ||
                user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_USER_INFO);
        }

        // 1. Khởi tạo đối tượng xe và bài đăng trước
        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName()).orElseThrow();
        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName()).orElseThrow();
        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);

        // Lưu xe vào DB trước
        bicycle = bicycleRepository.save(bicycle);

        BikeListing bikeListing = postingMapper.toBikeListing(request, bicycle);
        bikeListing.setSeller(user);

        // 2. Tính phí
        double listingFee = calculateListingFee(request.getPrice());

        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder().user(user).username(username).balance(0.0).type("User").build();
            return walletRepository.save(newWallet);
        });

        // 3. Nếu VÍ KHÔNG ĐỦ TIỀN -> Lưu nháp Waiting_Payment & Trả về VNPay
        if (wallet.getBalance() < listingFee) {
            bikeListing.setStatus("Waiting_Payment");
            BikeListing savedListing = bikeListingRepository.save(bikeListing);

            long amountNeeded = (long) Math.ceil(listingFee - wallet.getBalance());
            String customReturnUrl = vnpayReturnUrl + "?listingId=" + savedListing.getListingId();

            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|fee|" + savedListing.getListingId(),
                    customReturnUrl, null
            );

            return CreatePostingResponse.builder()
                    .listing(null)
                    .paymentUrl(paymentUrl)
                    .message("Số dư không đủ. Vui lòng thanh toán " + amountNeeded + " VND.")
                    .build();
        }

        // 4. Nếu VÍ ĐỦ TIỀN -> Trừ tiền ngay và đăng bài (Pending chờ duyệt)
        Wallet systemWallet = walletRepository.findByUsername("System")
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance() - listingFee);
        systemWallet.setBalance(systemWallet.getBalance() + listingFee);
        walletRepository.save(wallet);
        walletRepository.save(systemWallet);

        if (listingFee > 0) {
            walletTransactionService.createTransaction(wallet, listingFee, "ListingFee", "Phí đăng bài xe đạp");
        }

        bikeListing.setStatus("Pending");
        PostingResponse saved = postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));

        return CreatePostingResponse.builder()
                .listing(saved)
                .paymentUrl(null)
                .message("Đăng bài thành công! Đã trừ " + (long) listingFee + " VND từ ví.")
                .build();
    }

    // --- HÀM BỔ SUNG: DÀNH CHO USER BẤM THANH TOÁN LẠI TỪ KHO XE TRÊN FRONTEND ---
    @Transactional
    public CreatePostingResponse retryPayment(int listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        if (!"Waiting_Payment".equals(bikeListing.getStatus())) {
            throw new RuntimeException("Bài đăng này không ở trạng thái chờ thanh toán.");
        }

        double listingFee = calculateListingFee(bikeListing.getPrice());
        Wallet wallet = walletRepository.findByUsername(username).orElseThrow();

        if (wallet.getBalance() < listingFee) {
            long amountNeeded = (long) Math.ceil(listingFee - wallet.getBalance());
            String customReturnUrl = vnpayReturnUrl + "?listingId=" + listingId;
            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    username + "|fee|" + listingId,
                    customReturnUrl, null
            );
            return CreatePostingResponse.builder().paymentUrl(paymentUrl).build();
        } else {
            Wallet systemWallet = walletRepository.findByUsername("System").orElseThrow();
            wallet.setBalance(wallet.getBalance() - listingFee);
            systemWallet.setBalance(systemWallet.getBalance() + listingFee);
            walletRepository.save(wallet);
            walletRepository.save(systemWallet);

            walletTransactionService.createTransaction(wallet, listingFee, "ListingFee", "Phí đăng bài xe đạp #" + listingId);

            bikeListing.setStatus("Pending");
            PostingResponse saved = postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));
            return CreatePostingResponse.builder().listing(saved).build();
        }
    }

    // --- HÀM XỬ LÝ KHI VNPAY TRẢ VỀ THÀNH CÔNG (ĐƯỢC GỌI TỪ PAYMENT CONTROLLER) ---
    @Transactional
    public void confirmPaymentAndPublish(int listingId, String username, double vnpayAmount) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        if (!"Waiting_Payment".equals(bikeListing.getStatus())) {
            return; // Tránh xử lý trùng lặp
        }

        double listingFee = calculateListingFee(bikeListing.getPrice());

        Wallet userWallet = walletRepository.findByUsername(username).orElseThrow();
        Wallet systemWallet = walletRepository.findByUsername("System").orElseThrow();

        // 1. Nạp tiền VNPay vào ví User
        userWallet.setBalance(userWallet.getBalance() + vnpayAmount);
        walletRepository.save(userWallet);
        walletTransactionService.createTransaction(userWallet, vnpayAmount, "Fee_TopUp", "Nạp tiền VNPay để trả phí đăng bài");

        // 2. Trừ tiền ví User sang ví System
        userWallet.setBalance(userWallet.getBalance() - listingFee);
        systemWallet.setBalance(systemWallet.getBalance() + listingFee);
        walletRepository.save(userWallet);
        walletRepository.save(systemWallet);
        walletTransactionService.createTransaction(userWallet, listingFee, "ListingFee", "Phí đăng bài xe đạp #" + listingId);

        // 3. Cập nhật trạng thái bài đăng sang chờ Admin duyệt
        bikeListing.setStatus("Pending");
        bikeListingRepository.save(bikeListing);
    }

    // --- HÀM XÓA BÀI RÁC NẾU USER HỦY VNPAY ---
    @Transactional
    public void cancelPostingPayment(int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId).orElse(null);
        if (bikeListing != null && "Waiting_Payment".equals(bikeListing.getStatus())) {
            Bicycle bicycle = bikeListing.getBicycle();

            // Xóa BikeListing trước
            bikeListingRepository.delete(bikeListing);

            // Xóa luôn Bicycle để tránh rác DB
            if (bicycle != null) {
                bicycleRepository.delete(bicycle);
            }
        }
    }

    @Transactional
    public PostingResponse updatePosting(UpdatePostingRequest request, int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        Bicycle bicycle = bikeListing.getBicycle();
        postingMapper.updateBicycleFromRequest(request, bicycle);
        postingMapper.updateBikeListingFromRequest(request, bikeListing);
        bikeListing.setStatus("Pending");

        return postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));
    }

    @Transactional
    public PostingResponse getPostingById(int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        return postingMapper.toPostingResponse(bikeListing);
    }

    @Transactional
    public List<PostingResponse> getMyPostings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return bikeListingRepository.findBySeller(user)
                .stream().map(postingMapper::toPostingResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<PostingResponse> getAllPostings() {
        return bikeListingRepository.findAll()
                .stream().map(postingMapper::toPostingResponse).collect(Collectors.toList());
    }

    @Transactional
    public String deletePosting(int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        bikeListingRepository.delete(bikeListing);
        return "Bike listing deleted successfully";
    }

    public PostingResponse updatePostingStatus(int listingId, UpdatePostingStatusRequest request) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));
        bikeListing.setStatus(request.getStatus());
        return postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));
    }
}