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

        // Phí_Sàn lưu theo dạng % (ví dụ 5.0 = 5%)
        // Phí thực tế = giá bán xe × Phí_Sàn / 100
        double feePercent = systemConfigRepository.findByKey("Phí_Sàn")
                .map(SystemConfig::getValue)
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình Phí_Sàn trong hệ thống. Vui lòng thêm config key 'Phí_Sàn'."));
        double listingFee = request.getPrice() * feePercent / 100.0;
        log.info("[createPosting] feePercent={}, price={}, listingFee={}", feePercent, request.getPrice(), listingFee);

        Wallet wallet = walletRepository.findByUsername(username).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .username(username)
                    .balance(0.0)
                    .type("User")
                    .build();
            return walletRepository.save(newWallet);
        });

        if (wallet.getBalance() < listingFee) {
            long amountNeeded = (long) Math.ceil(listingFee - wallet.getBalance());
            String paymentUrl = vnPayService.createOrder(
                    amountNeeded,
                    "Thanh toan phi dang bai",
                    vnpayReturnUrl,
                    null
            );
            return CreatePostingResponse.builder()
                    .listing(null)
                    .paymentUrl(paymentUrl)
                    .message("Số dư ví không đủ. Vui lòng thanh toán " + amountNeeded + " VND để tiếp tục đăng bài.")
                    .build();
        }

        wallet.setBalance(wallet.getBalance() - listingFee);
        walletRepository.save(wallet);

        if (listingFee > 0) {
            walletTransactionService.createTransaction(wallet, listingFee, "ListingFee", "Phí đăng bài xe đạp");
        }

        // 5. Tạo bài đăng
        Brand brand = brandRepository.findByNameIgnoreCase(request.getBrandName())
                .orElseThrow();

        Category category = categoryRepository.findByNameIgnoreCase(request.getCategoryName())
                .orElseThrow();

        Bicycle bicycle = postingMapper.toBicycle(request, brand, category);

        BikeListing bikeListing = postingMapper.toBikeListing(request, bicycle);
        bikeListing.setSeller(user);
        bikeListing.setStatus("Pending");

        PostingResponse saved = postingMapper.toPostingResponse(bikeListingRepository.save(bikeListing));

        return CreatePostingResponse.builder()
                .listing(saved)
                .paymentUrl(null)
                .message("Đăng bài thành công. Phí đăng bài " + (long) listingFee + " VND đã được trừ từ ví.")
                .build();
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
