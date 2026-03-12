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
    private final PostingMapper postingMapper;
    private final IUserRepository userRepository;
    private final IBrandRepository brandRepository;
    private final ICategoryRepository categoryRepository;
    private final IWalletRepository walletRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final WalletTransactionService walletTransactionService;
    private final VNPayService vnPayService;

    public double calculateListingFee(double price) {
        // Lấy phí từ DB, nếu chưa cấu hình thì mặc định thu 5%
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

        // 1. Kiểm tra ví
        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(username)
                    .balance(0.0)
                    .type("User")
                    .build();
            return walletRepository.save(newWallet);
        });

        // 2. Tính phí
        double listingFee = calculateListingFee(request.getPrice());

        // 3. Khởi tạo đối tượng
        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName()).orElseThrow();
        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName()).orElseThrow();
        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);
        BikeListing bikeListing = postingMapper.toBikeListing(request, bicycle);
        bikeListing.setSeller(user);

        // 4. Nếu VÍ KHÔNG ĐỦ TIỀN -> Trả về link nạp tiền
        if (wallet.getBalance() < listingFee) {
            bikeListing.setStatus("Waiting_Payment");
            BikeListing savedListing = bikeListingRepository.save(bikeListing);

            long amountNeeded = (long) Math.ceil(listingFee - wallet.getBalance());
            // Gắn ID bài đăng vào URL để khi thanh toán xong VNPay gọi về có thể update đúng bài
            String customReturnUrl = vnpayReturnUrl + "?listingId=" + savedListing.getListingId();

            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    "Nap tien de dang bai #" + savedListing.getListingId(),
                    customReturnUrl,
                    null
            );

            return CreatePostingResponse.builder()
                    .listing(null)
                    .paymentUrl(paymentUrl)
                    .message("Số dư không đủ. Vui lòng thanh toán " + amountNeeded + " VND.")
                    .build();
        }

        // 5. Nếu VÍ ĐỦ TIỀN -> Trừ tiền và đăng
        wallet.setBalance(wallet.getBalance() - listingFee);
        walletRepository.save(wallet);

        if (listingFee > 0) {
            walletTransactionService.createTransaction(wallet, listingFee, "ListingFee", "Phí đăng bài xe đạp");
        }

        bikeListing.setStatus("Pending"); // Trạng thái chờ Admin duyệt
        PostingResponse saved = postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));

        return CreatePostingResponse.builder()
                .listing(saved)
                .paymentUrl(null)
                .message("Đăng bài thành công! Đã trừ " + (long) listingFee + " VND từ ví.")
                .build();
    }

//    @Transactional
//    public CreatePostingResponse createPosting(CreatePostingRequest request) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
//        String username = authentication.getName();
//        Users user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        if (user.getPhone() == null || user.getPhone().trim().isEmpty() ||
//            user.getAddress() == null || user.getAddress().trim().isEmpty()) {
//            throw new AppException(ErrorCode.MISSING_USER_INFO);
//        }
//
//        // Phí_Sàn lưu theo dạng % (ví dụ 5.0 = 5%)
//        // Phí thực tế = giá bán xe × Phí_Sàn / 100
//        double feePercent = systemConfigRepository.findByKey("Phí_Sàn")
//                .map(SystemConfig::getValue)
//                .orElseThrow(() -> new RuntimeException("Chưa cấu hình Phí_Sàn trong hệ thống. Vui lòng thêm config key 'Phí_Sàn'."));
//        double listingFee = request.getPrice() * feePercent / 100.0;
//        log.info("[createPosting] feePercent={}, price={}, listingFee={}", feePercent, request.getPrice(), listingFee);
//
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
//        if (wallet.getBalance() < listingFee) {
//            long amountNeeded = (long) Math.ceil(listingFee - wallet.getBalance());
//            String paymentUrl = vnPayService.createOrder(
//                    amountNeeded,
//                    "Thanh toan phi dang bai",
//                    vnpayReturnUrl,
//                    null
//            );
//            return CreatePostingResponse.builder()
//                    .listing(null)
//                    .paymentUrl(paymentUrl)
//                    .message("Số dư ví không đủ. Vui lòng thanh toán " + amountNeeded + " VND để tiếp tục đăng bài.")
//                    .build();
//        }
//
//        wallet.setBalance(wallet.getBalance() - listingFee);
//        walletRepository.save(wallet);
//
//        if (listingFee > 0) {
//            walletTransactionService.createTransaction(wallet, listingFee, "ListingFee", "Phí đăng bài xe đạp");
//        }
//
//        // 5. Tạo bài đăng
//        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName())
//                .orElseThrow();
//
//        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName())
//                .orElseThrow();
//
//        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);
//
//        BikeListing bikeListing = postingMapper.toBikeListing(request, bicycle);
//        bikeListing.setSeller(user);
//        bikeListing.setStatus("Pending");
//
//        PostingResponse saved = postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));
//
//        return CreatePostingResponse.builder()
//                .listing(saved)
//                .paymentUrl(null)
//                .message("Đăng bài thành công. Phí đăng bài " + (long) listingFee + " VND đã được trừ từ ví.")
//                .build();
//    }

    @Transactional
    public String confirmPaymentAndPublish(int listingId) {
        BikeListing bikeListing = bikeListingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.BIKE_LISTING_NOT_FOUND));

        if (!"Waiting_Payment".equals(bikeListing.getStatus())) {
            return "Bài đăng này đã được xử lý thanh toán trước đó.";
        }

        // Cập nhật trạng thái chờ Admin duyệt
        bikeListing.setStatus("Pending");
        bikeListingRepository.save(bikeListing);

        return "Thanh toán thành công. Bài đăng đã chuyển sang chờ duyệt!";
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
                .stream()
                .map(postingMapper::toPostingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PostingResponse> getAllPostings() {
        return bikeListingRepository.findAll()
                .stream()
                .map(postingMapper::toPostingResponse)
                .collect(Collectors.toList());
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
